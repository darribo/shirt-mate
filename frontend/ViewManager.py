# ViewManager.py
import gi

gi.require_version('Gtk', '4.0') # establecer versión
from gi.repository import Gtk, Gio, GObject, GLib

class ViewManager:
    def __init__(self):
        self.stack = Gtk.Stack()
        self.views = {}

    def add_view(self, name, view):
        self.stack.add_named(view.widget, name)
        self.views[name] = view

    def show_view(self, name):
        self.stack.set_visible_child_name(name)

    def get_stack(self):
        return self.stack

    def get_view(self, name):
        return self.views.get(name)

    def get_view_name(self, view):
        for name, obj in self.views.items():
            if obj is view:
                return name
        return None  # Si no lo encuentra
