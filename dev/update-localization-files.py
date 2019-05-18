import sys, os 
import shutil

MASSAGES = "core/src/main/resources/com/egoal/darkestpixeldungeon/messages/"
LOCALIZATIONS = "dev/messages/"

def copy_source_out():
	for fld in os.listdir(MASSAGES):
		filename = "{}.properties".format(fld)
		file = "{}{}/{}".format(MASSAGES, fld, filename)

		shutil.copy(file, LOCALIZATIONS+ filename)

def copy_source_in():
	FILE_FOLDER = "../msg/"
	FILE_TAGS = ["actors", "items", "levels", "misc", "plants", "scenes", "ui", "windows"]
	files = {}
	for file in os.listdir(FILE_FOLDER):
		for tag in FILE_TAGS:
			if tag in file:
				files[tag] = FILE_FOLDER+ file
				break
	
	for tag, file in files.items():
		MASSAGES
		# print(file, "{}{}/{}.properties".format(MASSAGES, tag, tag))
		shutil.copy(file, "{}{}/{}.properties".format(MASSAGES, tag, tag))

	print("message in.")

copy_source_in()
