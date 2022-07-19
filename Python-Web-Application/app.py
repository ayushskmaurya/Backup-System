from flask import Flask, render_template, redirect, url_for, session, jsonify
from tkinter import *
from tkinter import filedialog
from PIL import Image, ImageTk
import os

app = Flask(__name__)
app.secret_key = "1234"


# Listing all the files and folders of the selected directory.
def pc_files(abs_path, path, pc_dir_list):
	files = os.listdir(abs_path)
	for file in files:
		abs_file_path = os.path.join(abs_path, file)
		file_path = os.path.join(path, file)
		isDir, last_modified = None, None

		if os.path.isdir(abs_file_path):
			isDir = True
			pc_files(abs_file_path, file_path, pc_dir_list)
		else:
			isDir = False
			last_modified = os.path.getmtime(abs_file_path)

		pc_dir_list[file_path.replace("\\", "/")] = {'isDir': isDir, 'last_modified': last_modified}


@app.route("/")
def home():
	dir_path = None
	if 'show_path' in session and session['show_path']:
		dir_path = session['dir_path']
		session['show_path'] = False
	return render_template("index.html", dir_path=dir_path)


@app.route("/browse")
def browse():
	root = Tk()
	root.title("Backup System")
	root.geometry("270x0")
	root.iconphoto(False, ImageTk.PhotoImage(Image.open('static\\logo\\logo.ico')))

	dir_path = filedialog.askdirectory().strip()
	if len(dir_path) != 0:
		session['dir_path'] = dir_path
		session['show_path'] = True
	
	root.destroy()
	root.mainloop()
	return redirect(url_for('home'))


@app.route("/get_pc_dir_list")
def get_pc_dir_list():
	pc_dir_list = {}
	pc_files(session['dir_path'], "", pc_dir_list)
	return jsonify(pc_dir_list)


if __name__ == '__main__':
	app.run(debug=True)
