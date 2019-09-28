import sys, os
import cv2
import numpy as np

def crop_immage(imgfile, row_size, col_size, out_dir):
    print(f"crop {imgfile} to tile {row_size}x{col_size}")
    img = cv2.imread(imgfile, -1)
    print(img.shape)
    assert(img.shape[0]% row_size==0 and img.shape[1]% col_size==0)

    rows, cols = img.shape[0]// row_size, img.shape[1]// col_size
    i = 0
    for r in range(rows):
        for c in range(cols):
            subimg = img[(r* row_size) :((r+1)* row_size), (c* col_size):((c+1)* col_size), :]
            if np.sum(subimg[:, :, 3])<=0:
                # skip null image
                continue

            outfile = os.path.join(out_dir, f"{r* cols+ c}.png")
            cv2.imwrite(outfile, subimg)
            i += 1

    print(f"done, {i}/{rows* cols} subimages saved.")

if __name__ == "__main__":
    if len(sys.argv)< 4:
        print("imgcrop.py [imgfile] [row-size] [cols-size] [out-dir?]")
        sys.exit()

    out_dir = sys.argv[4] if len(sys.argv)>= 5 else "."
    crop_immage(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]), out_dir)
