import sys, os
import random

TNONE, TWALL, TFLOOR, TDOOR = 0, 1, 2, 3
TILE_CHARS = { TNONE: ' ', TWALL: '#', TFLOOR: '.', TDOOR: 'D', }

class Rect:
    def __init__(self, x1, x2, y1, y2):
        self.x1, self.x2, self.y1, self.y2 = x1, x2, y1, y2

    def size(self):
        return (self.x2-self.x1+1, self.y2-self.y1+1)

    def inner(self, i):
        return Rect(self.x1+i, self.x2-i, self.y1+i, self.y2-i)

class Map:
    def __init__(self, w, h):
        self.width, self.height = w, h
        self.tiles = [[TNONE for x in range(w)] for y in range(h)]

    def print(self):
        for y in range(self.height):
            print(''.join([TILE_CHARS[t] for t in self.tiles[y]]))

    def set_rect(self, rect, tile):
        for y in range(rect.y1, rect.y2+1):
            for x in range(rect.x1, rect.x2+1):
                self.tiles[y][x] = tile
    
    def at(self, x, y):
        if x<0 or x>=self.width or y<0 or y>=self.height:
            return None
        return self.tiles[y][x]

    def NEIGHBOR_8(self, x, y):
        return [(x-1, y-1), (x, y-1), (x+1, y-1), (x-1, y), (x+1, y), 
            (x-1, y+1), (x, y+1), (x+1, y+1), ]

class LocalMapRect(Map):
    def generate(self):
        r = Rect(0, self.width-1, 0, self.height-1)
        self.set_rect(r, TWALL)
        self.set_rect(r.inner(1), TFLOOR)

class LocalMapCave(Map):
    def generate(self):
        for y in range(self.height):
            for x in range(self.width):
                self.tiles[y][x] = TFLOOR if random.random()<.3 else TWALL

        for i in range(3):
            newtiles = [[TNONE for x in range(self.width)] for y in range(self.height)]
            for y in range(self.height):
                for x in range(self.width):
                    nw = 0
                    for pt in self.NEIGHBOR_8(x, y):
                        if self.at(pt[0], pt[1])==TWALL:
                            nw += 1

                    newtiles[y][x] = TWALL if nw>=5 else TFLOOR
            
            self.tiles = newtiles

class DungeonMap(Map):
    def generate(self):
        place_first_room()

    def place_first_room(self):
        pass        

m = LocalMapCave(16, 16)
m.generate()
m.print()
