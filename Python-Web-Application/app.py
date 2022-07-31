from flask import Flask, render_template, redirect, request, url_for, jsonify
from base import Base
import os
import shutil
import json
import base64
import random

app = Flask(__name__)
app.secret_key = str(random.randint(1111, 9999)).encode()
base = Base()


# Listing all the files and folders of the selected directory.
def pc_files(abs_path, path, pc_dir_list):
	files = os.listdir(abs_path)
	for file in files:
		abs_file_path = os.path.join(abs_path, file)
		file_path = os.path.join(path, file)
		is_dir, last_modified, size = None, None, None

		if os.path.isdir(abs_file_path):
			is_dir = True
			pc_files(abs_file_path, file_path, pc_dir_list)
		else:
			is_dir = False
			last_modified = int(os.path.getmtime(abs_file_path))
			size = os.path.getsize(abs_file_path)

		pc_dir_list[file_path.replace("\\", "/")] = {'is_dir': is_dir, 'last_modified': last_modified, 'size': size}


@app.route("/")
def home():
	dir_path = None
	if base.get_show_dir_path():
		dir_path = base.get_dir_path()
		base.set_show_dir_path(False)
	return render_template("index.html", dir_path=dir_path)


@app.route("/dir_path", methods=['POST'])
def dir_path():
	entered_dir_path = request.form['dir_path'].strip()
	if len(entered_dir_path) != 0:
		base.set_dir_path(entered_dir_path)
		base.set_show_dir_path(True)
	return redirect(url_for('home'))


# Retrieving list of all files & folders of PC.
@app.route("/get_pc_dir_list")
def get_pc_dir_list():
	pc_dir_list = {}
	dir_path = base.get_dir_path()
	if dir_path is not None and os.path.exists(dir_path) and os.path.isdir(dir_path):
		pc_files(dir_path, "", pc_dir_list)
	return jsonify(pc_dir_list)


# Deleting unwanted files.
@app.route("/delete_files", methods=['POST'])
def delete_files():
	pc_dir_list = json.loads(request.form['pc_dir_list'])
	files_to_keep = json.loads(request.form['files_to_keep'])

	for file in pc_dir_list:
		if file not in files_to_keep:
			file_path = os.path.join(base.get_dir_path(), file)
			if os.path.exists(file_path):
				if os.path.isdir(file_path):
					shutil.rmtree(file_path)
				else:
					os.remove(file_path)
	return "1"


# Taking backup of the files.
@app.route("/backup_files", methods=['POST'])
def backup_files():
	file_path = request.form['file_path']
	enc_file_chunk_str = request.form['enc_file_chunk_str']
	is_dir = request.form['is_dir']
	abs_file_path = os.path.join(base.get_dir_path(), file_path)

	if is_dir == "true":
		if not os.path.exists(abs_file_path):
			os.makedirs(abs_file_path)
	else:
		dirname = os.path.dirname(abs_file_path)
		if not os.path.exists(dirname):
			os.makedirs(dirname)
		with open(abs_file_path, "ab") as file_to_backup:
			file_to_backup.write(base64.b64decode(enc_file_chunk_str))
	
	return "1"


if __name__ == '__main__':
	app.run(host=base.get_host())
