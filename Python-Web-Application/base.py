class Base:
    HOST = 'localhost'
    DIR_PATH = None
    SHOW_DIR_PATH = False

    def get_host(self):
        return self.HOST

    def get_dir_path(self):
        return self.DIR_PATH

    def set_dir_path(self, dir_path):
        self.DIR_PATH = dir_path

    def get_show_dir_path(self):
        return self.SHOW_DIR_PATH

    def set_show_dir_path(self, show_dir_path):
        self.SHOW_DIR_PATH = show_dir_path
