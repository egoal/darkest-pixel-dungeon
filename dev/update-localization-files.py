import sys, os 
import shutil

MASSAGES = "core/src/main/resources/com/egoal/darkestpixeldungeon/messages/"
LOCALIZATIONS = "dev/messages/"

def copy_source_out():
	for fld in os.listdir(MASSAGES):
		filename = "{}.properties".format(fld)
		file = "{}{}/{}".format(MASSAGES, fld, filename)

		shutil.copy(file, LOCALIZATIONS+ filename)

def copy_source_in(locsrc, loctgt):
	FILE_FOLDER = "../msg/"
	FILE_TAGS = ["actors", "items", "levels", "misc", "plants", "scenes", "ui", "windows"]
	files = {}
	for file in os.listdir(FILE_FOLDER):
		if locsrc in file:
			for tag in FILE_TAGS:
				if tag in file:
					files[tag] = FILE_FOLDER+ file
					break
	
	for tag, file in files.items():
		# MASSAGES
		shutil.copy(file, "{}{}/{}{}.properties".format(MASSAGES, tag, tag, loctgt))
		# print(file, " -> ", "{}{}/{}{}.properties".format(MASSAGES, tag, tag, loctgt))

	print(f"message in: {locsrc} -> {loctgt}.")

locs = { "_en": "_en", "_zh_HK": "_zh_TW", "_ru": "_ru" }

for ls, lt in locs.items():
	copy_source_in(ls, lt)
