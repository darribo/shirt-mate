import gi
from typing import Callable

from CollaboratorModel import CollaboratorModel
from Presenter import Presenter
from RaffleModel import RaffleModel
from ShirtModel import ShirtModel
from ShirtsView import ShirtsView
from CollaboratorsView import CollaboratorsView
from ShirtTypesView import ShirtTypesView
from ViewManager import ViewManager

gi.require_version('Gtk', '4.0') # establecer versión
from gi.repository import Gtk, Gio, GObject, GLib

APPLICATION_ID: str = 'es.udc.fic.misProyectos.Camisetas' #identificador a nivel global de la aplicación


def on_application_activate(app: Gtk.Application):
    win = Gtk.ApplicationWindow(application=app)
    shirt_model = ShirtModel()
    collaborator_model = CollaboratorModel()
    raffle_model = RaffleModel()

    view_manager = ViewManager()

    # PANTALLA DE CAMISETAS
    shirts_view = ShirtsView(win)
    view_manager.add_view("Camisetas", shirts_view)

    # PANTALLA DE PEÑAS
    shirt_types_view = ShirtTypesView(win)
    view_manager.add_view("Peñas", shirt_types_view)

    # PANTALLA DE COLABORADORES
    collaborators_view = CollaboratorsView(win)
    view_manager.add_view("Inversores", collaborators_view)

    presenter = Presenter(shirts_view, shirt_model, collaborator_model, raffle_model, view_manager)

    shirts_view.set_presenter(presenter)
    shirt_types_view.set_presenter(presenter)
    collaborators_view.set_presenter(presenter)

    # Añadir el stack como único hijo de la ventana
    win.set_child(view_manager.get_stack())
    app.add_window(win)
    win.present()


def run(application_id: str, on_activate: Callable):
    app = Gtk.Application(application_id=application_id)
    app.connect('activate', on_activate)
    app.run(None)

if __name__ == '__main__':
    run(APPLICATION_ID, on_application_activate)