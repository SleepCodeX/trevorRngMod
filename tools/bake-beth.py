#!/usr/bin/env python3
"""Bake the pinned CC0 KayKit Rogue into a small native-renderer asset.

Requires numpy and Pillow only while authoring. No glTF loader, animation library,
network request or image processing runs in Minecraft. See docs/3D-MODELS.md.
"""
import argparse
import gzip
import hashlib
import io
import json
from pathlib import Path
import struct

import numpy as np
from PIL import Image

SHA256 = 'e825437cd4d2ee9c1960b517a74a69101e33eb409ae7fa8cedc7134a998fbb7d'
CLIPS = ['Idle', 'Walking_A', 'Interact']
FPS = 60


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('rogue_glb', type=Path)
    parser.add_argument('--output', type=Path, default=Path('src/main/resources/assets/trevorroll'))
    a = parser.parse_args()
    data = a.rogue_glb.read_bytes()
    assert hashlib.sha256(data).hexdigest() == SHA256, 'Use the documented unmodified source GLB'
    assert data[:4] == b'glTF' and struct.unpack_from('<I', data, 4)[0] == 2
    length = struct.unpack_from('<I', data, 12)[0]
    g = json.loads(data[20:20 + length])
    start = 20 + length
    binary = data[start + 8: start + 8 + struct.unpack_from('<I', data, start)[0]]

    def accessor(index):
        v = g['accessors'][index]
        view = g['bufferViews'][v['bufferView']]
        dtype = {5121: 'u1', 5123: '<u2', 5125: '<u4', 5126: '<f4'}[v['componentType']]
        size = {'SCALAR': 1, 'VEC2': 2, 'VEC3': 3, 'VEC4': 4, 'MAT4': 16}[v['type']]
        offset = view.get('byteOffset', 0) + v.get('byteOffset', 0)
        stride = view.get('byteStride', np.dtype(dtype).itemsize * size)
        return np.ndarray((v['count'], size), dtype=dtype, buffer=binary,
                          offset=offset, strides=(stride, np.dtype(dtype).itemsize)).copy()

    nodes = g['nodes']
    parent = {child: i for i, n in enumerate(nodes) for child in n.get('children', [])}
    skin = g['skins'][0]
    joints = list(skin['joints'])
    inverse = accessor(skin['inverseBindMatrices']).reshape(-1, 4, 4).transpose(0, 2, 1)
    cape = next(i for i, n in enumerate(nodes) if n.get('name') == 'Rogue_Cape')
    joints.append(cape)
    conversion = np.diag([14., -14., 14., 1.])
    conversion_inv = np.linalg.inv(conversion)

    def rotation(q):
        x, y, z, w = q / np.linalg.norm(q)
        return np.array([[1-2*(y*y+z*z), 2*(x*y-z*w), 2*(x*z+y*w)],
                         [2*(x*y+z*w), 1-2*(x*x+z*z), 2*(y*z-x*w)],
                         [2*(x*z-y*w), 2*(y*z+x*w), 1-2*(x*x+y*y)]])

    def sample(values, times, seconds, quaternion):
        hi = min(int(np.searchsorted(times, seconds, side='right')), len(times)-1)
        lo = max(0, hi-1)
        t = np.clip((seconds-times[lo]) / max(1e-10, times[hi]-times[lo]), 0, 1)
        x, y = values[lo], values[hi]
        if quaternion:
            dot = float(np.dot(x, y))
            if dot < 0:
                y, dot = -y, -dot
            if dot < .9995:
                angle = np.arccos(np.clip(dot, -1, 1))
                return (np.sin((1-t)*angle)*x + np.sin(t*angle)*y) / np.sin(angle)
        return x*(1-t) + y*t

    def bake_clip(animation):
        channels = []
        duration = 0
        for channel in animation['channels']:
            sampler = animation['samplers'][channel['sampler']]
            assert sampler.get('interpolation', 'LINEAR') == 'LINEAR'
            times, values = accessor(sampler['input']).ravel(), accessor(sampler['output'])
            duration = max(duration, float(times[-1]))
            channels.append((channel['target']['node'], channel['target']['path'], times, values))
        frames = int(np.ceil(duration*FPS)) + 1
        result = []
        for seconds in np.linspace(0, duration, frames):
            trs = [[np.array(n.get('translation', [0, 0, 0]), dtype=float),
                    np.array(n.get('rotation', [0, 0, 0, 1]), dtype=float),
                    np.array(n.get('scale', [1, 1, 1]), dtype=float)] for n in nodes]
            for node, path, times, values in channels:
                trs[node][{'translation': 0, 'rotation': 1, 'scale': 2}[path]] = sample(values, times, seconds, path == 'rotation')
            local = []
            for t, r, s in trs:
                m = np.eye(4)
                m[:3, :3] = rotation(r) * s
                m[:3, 3] = t
                local.append(m)
            world = {}

            def global_matrix(i):
                if i not in world:
                    world[i] = global_matrix(parent[i]) @ local[i] if i in parent else local[i]
                return world[i]

            matrices = []
            for i, node in enumerate(joints):
                m = global_matrix(node)
                if i < len(inverse):
                    m = m @ inverse[i]
                matrices.append((conversion @ m @ conversion_inv)[:3].ravel())
            result.append(matrices)
        return duration, np.array(result, dtype='>f4')

    vertices, indices = [], []
    for node_index, node in enumerate(nodes):
        if 'mesh' not in node or ('skin' not in node and node_index != cape):
            continue  # Do not bake the pack's optional weapons into the greeting.
        for primitive in g['meshes'][node['mesh']]['primitives']:
            assert primitive.get('mode', 4) == 4
            attrs = primitive['attributes']
            pos, normal, uv = [accessor(attrs[k]) for k in ['POSITION', 'NORMAL', 'TEXCOORD_0']]
            weights = accessor(attrs['WEIGHTS_0']) if 'skin' in node else np.tile([1., 0, 0, 0], (len(pos), 1))
            bone = accessor(attrs['JOINTS_0']) if 'skin' in node else np.full((len(pos), 4), len(joints)-1)
            assert np.allclose(weights.sum(axis=1), 1, atol=1e-4)
            base = len(vertices)
            for p, n, u, b, w in zip(pos, normal, uv, bone, weights):
                p = (conversion @ np.append(p, 1))[:3]
                n = n * [1, -1, 1]
                vertices.append((p, n, u, b, w))
            # The Y-axis reflection changes winding; preserve outward facing triangles.
            triangles = accessor(primitive['indices']).reshape(-1, 3)[:, [0, 2, 1]]
            indices.extend((triangles + base).ravel())

    output = io.BytesIO()
    output.write(struct.pack('>5I', 0x54524248, 1, len(joints), len(vertices), len(indices)))
    for p, n, uv, bone, weights in vertices:
        output.write(struct.pack('>8f4H4f', *p, *n, *uv, *bone, *weights))
    output.write(struct.pack('>' + 'I'*len(indices), *indices))
    output.write(struct.pack('>I', len(CLIPS)))
    for name in CLIPS:
        duration, frames = bake_clip(next(x for x in g['animations'] if x['name'] == name))
        output.write(struct.pack('>fI', duration, len(frames)))
        output.write(frames.tobytes())
        print(name, 'seconds', round(duration, 3), 'frames', len(frames))
    target = a.output / 'models/beth.bin.gz'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.compress(output.getvalue(), compresslevel=9, mtime=0))
    image = g['images'][0]
    view = g['bufferViews'][image['bufferView']]
    image_data = binary[view.get('byteOffset', 0):view.get('byteOffset', 0)+view['byteLength']]
    texture = a.output / 'textures/gui/beth.png'
    texture.parent.mkdir(parents=True, exist_ok=True)
    # Keep the authored gradient atlas intact; no real-time colour/image generation.
    with Image.open(io.BytesIO(image_data)) as im:
        im.save(texture, optimize=True)
    print('vertices', len(vertices), 'triangles', len(indices)//3, 'joints', len(joints))
    print(target, target.stat().st_size, 'bytes;', texture, texture.stat().st_size, 'bytes')


if __name__ == '__main__':
    main()
