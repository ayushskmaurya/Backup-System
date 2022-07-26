from flask import Flask, render_template, redirect, request, url_for, jsonify
from base import Base
import os

app = Flask(__name__)
app.secret_key = "1234"
base = Base()


# Listing all the files and folders of the selected directory.
def pc_files(abs_path, path, pc_dir_list):
	files = os.listdir(abs_path)
	for file in files:
		abs_file_path = os.path.join(abs_path, file)
		file_path = os.path.join(path, file)
		is_dir, last_modified = None, None

		if os.path.isdir(abs_file_path):
			is_dir = True
			pc_files(abs_file_path, file_path, pc_dir_list)
		else:
			is_dir = False
			last_modified = int(os.path.getmtime(abs_file_path))

		pc_dir_list[file_path.replace("\\", "/")] = {'is_dir': is_dir, 'last_modified': last_modified}


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


if __name__ == '__main__':
	app.run(debug=True, host=base.get_host())
