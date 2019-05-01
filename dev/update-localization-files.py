import sys, os 
import shutil

MASSAGES = "core/src/main/resources/com/egoal/darkestpixeldungeon/messages/"
LOCALIZATIONS = "dev/messages/"

def copy_source_out():
	for fld in os.listdir(MASSAGES):
		filename = "{}.properties".format(fld)
		file = "{}{}/{}".format(MASSAGES, fld, filename)

		shutil.copy(file, LOCALIZATIONS+ filename)

copy_source_out()
