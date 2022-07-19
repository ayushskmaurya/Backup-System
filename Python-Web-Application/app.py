from flask import Flask, render_template, redirect, url_for, session
from tkinter import *
from tkinter import filedialog
from PIL import Image, ImageTk

app = Flask(__name__)
app.secret_key = "1234"


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


if __name__ == '__main__':
	app.run(debug=True)
