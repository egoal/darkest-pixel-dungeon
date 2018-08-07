import sys, os
import random

import numpy as np
import matplotlib.pyplot as plt

MAP_WIDTH, MAP_HEIGHT = 64, 64, 

class Room:
    def __init__(self, x1, y1, x2, y2):
        self.x1, self.y1, self.x2, self.y2 = x1, y1, x2, y2
    
    def setAttributes(self, connecters, locked=False):
        self.connecters = connecters
        self.locked = locked

    def moveTo(self, x1, y1):
        dx, dy = x1-self.x1, y1-self.y1
        self.x1 += dx
        self.x2 += dx
        self.y1 += dy
        self.y2 += dy

    def isValid(self):
        return self.x1<self.x2 and self.y1<self.y2

    def random(self, inner=1):
        return (random.randint(self.x1+inner, self.x2-inner), 
            random.randint(self.y1+inner, self.y2-inner))

    def intersect(self, other, ):
        return not (self.x1>other.x2 or self.x2<other.x1 or self.y1>other.y2 or self.y2<other.y1)

    def w(self):
        return self.x2-self.x1+1
    def h(self):
        return self.y2-self.y1+1

    def p(self):
        return 'room[{}, {}, {}, {}] with {} connectors is {}'.format(
            self.x1, self.y1, self.x2, self.y2, self.connecters, 'locked' if self.locked else 'not locked')

class MG:
    def __init__(self):
        self.map = np.zeros((MAP_HEIGHT, MAP_WIDTH, ), dtype=np.int)

    def generate(self):
        self._initRooms()
        if not self._placeRooms():
            print('room placement failed.')
            return False
    
        return True

    def show(self):
        plt.imshow(self.map, cmap='gray')
        plt.show()

    def _initRooms(self):
        self.rooms = []

        # init types
        cntRooms = random.randint(8, 12)
        cntLocked = random.randint(1, 3)
        
        print('{} rooms with {} locked is going to spawned.'.format(cntRooms, cntLocked))
        
        for i in range(cntRooms):
            locked = i<cntLocked
            connects = 1 if locked else random.randint(1, 4)

            w, h = random.randint(4, 8), random.randint(4, 8)
            r = Room(0, 0, w, h)
            r.setAttributes(connects, locked)

            self.rooms.append(r)
            print(r.p())

    def _placeRooms(self):
        MAX_TRY = 10
        for __i in range(MAX_TRY):
            print('{} attempt to place rooms...'.format(__i))
            self.map[:, :] = 0
            roomsRest = self.rooms
            roomsAdded = self.rooms

            while len(roomsRest)>0:
                print('placing... {}/{} rooms rest/added'.format(len(roomsRest), len(roomsAdded)))
                i = random.randint(0, len(roomsRest)-1)
                r = roomsRest[i]
                del roomsRest[i]

                # random place
                placed = False
                for __j in range(MAX_TRY):
                    x, y = random.randint(0, MAP_WIDTH-r.w()), random.randint(0, MAP_HEIGHT-r.h())
                    r.moveTo(x, y)
                    canPlace = True
                    for rm in roomsAdded:
                        if rm.intersect(r):
                            canPlace = False
                            break

                    if canPlace:
                        # place a room
                        self.map[r.y1: r.y2+1, r.x1: r.x2+1] = 1
                        placed = True
                        break

                if not placed:
                    print('room place failed after {} rooms added.'.format(len(roomsAdded)))
                    print(self.map)
                    break
                roomsAdded.append(r)

            if len(roomsRest)==0:
                return True

        return False

if __name__=='__main__':
    mg = MG()
    if mg.generate():
        mg.show()
    else:
        print('map generation failed.')
