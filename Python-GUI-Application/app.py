from tkinter import *
from PIL import ImageTk, Image

root = Tk()
root.title("Backup System")
root.iconphoto(True, ImageTk.PhotoImage(Image.open("logo\\titlebar_icon.jpg")))
root.geometry("500x300")

root.mainloop()
