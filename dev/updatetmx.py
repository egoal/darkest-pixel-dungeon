import os, sys
import re

'''
update associated file after edit with Tiled
'''

TMX_PATH = 'tmx/'
DATA_PATH = '../core/src/main/assets/data/'

cnt = 0

for tmx in os.listdir(TMX_PATH):
    if os.path.isfile(TMX_PATH+ tmx):
        filename, suffix = os.path.splitext(tmx)
        if suffix=='.tmx':
            print('{} -> {}.map'.format(tmx, filename))

            with open(TMX_PATH+tmx) as fin, open(DATA_PATH+filename+'.map', 'w') as fout:
                whfound = False
                for line in fin.readlines():
                    if line.strip().startswith('<'):
                        if whfound:
                            continue

                        wh = re.findall(r'width=\"(\d+)\" height=\"(\d+)\"', line)
                        if wh:
                            fout.write('{} {}\n'.format(wh[0][0], wh[0][1]))
                            whfound = True
                    else:
                        eles = line.strip().split(',')
                        vals = []
                        for e in eles:
                            if len(e):
                                vals.append(int(e)-1)

                        fout.write(' '.join(str(v) for v in vals)+'\n')

            cnt += 1

print('>> done, {} files updated.'.format(cnt))
