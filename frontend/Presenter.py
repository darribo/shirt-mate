import os
from decimal import Decimal
from functools import partial
from PIL import Image

import gi
import threading

gi.require_version('Gtk', '4.0') # establecer versión
from gi.repository import GLib

class Presenter:
    def __init__(self, initial_view, shirt_model, collaborator_model, raffle_model, view_manager):

        self.view = None
        self.shirt_model = shirt_model
        self.collaborator_model = collaborator_model
        self.raffle_model = raffle_model
        self.view_manager = view_manager

        self.is_loading = None
        self.last_operation = None

        self.current_page = None
        self.exist_more_items = False

        self.pending_calls = 0

        self.shirt_form_sizes = ["XS", "S", "M", "L", "XL"]

        self.shirts = None
        self.shirt = None
        self.shirt_sale_price = None

        self.shirt_filter_shirt_types = None
        self.shirt_filter_collaborators = None

        self.update_shirt_types = None
        self.update_shirt_collaborators = None


        self.convinced_friends = None

        self.shirt_responsible = None
        self.shirt_type_responsible = None

        self.shirt_types = None
        self.shirt_type = None

        self.collaborators = None
        self.collaborator = None

        self.shirt_form_option = None
        self.shirt_type_form_option = None
        self.collaborator_form_option = None
        self.raffle_form_option = None
        self.level_form_option = None


        self.raffle = None
        self.level = None
        self.winner = None

        self.shirts_view = initial_view

        self.initialize_view(initial_view)

    def process_image(self, path):
        # Comprobar extensión válida
        valid_extensions = ('.jpg', '.jpeg', '.png')
        if not path.lower().endswith(valid_extensions):
            raise ValueError("Formato no soportado. Solo se admiten JPG, JPEG o PNG.")

        try:
            # Intentar abrir la imagen
            with Image.open(path) as img:
                img.verify()  # Verifica que el archivo es una imagen válida
        except Exception as e:
            raise ValueError(f"Error al abrir la imagen: {e}")

        # Extraer el nombre del archivo sin la ruta ni extensión
        file_name = os.path.splitext(os.path.basename(path))[0]
        return file_name

    # Función para inicializar una vista
    def initialize_view(self, view):
        self.view = view
        self.is_loading = False
        self.last_operation = None
        self.current_page = 0
        self.pending_calls = 0
        view_name = self.view_manager.get_view_name(view)
        self.view_manager.show_view(view_name)
        self.build_menu()

        self.load_list(view)



    def load_list(self, view):
        view_name = self.view_manager.get_view_name(view)
        if view_name == "Camisetas":
            self.view.clear_shirt_details_box()
            self.load_shirts()

        elif view_name == "Peñas":
            self.view.clear_shirt_type_details_box()
            self.load_shirt_types()

        elif view_name == "Inversores":
            self.view.clear_collaborator_details_box()
            self.load_total_profit()


    # Función para que un hilo haga una función y no interrumpa la interfaz
    def run_in_thread(self, func, callback=None, *args):
        if not self.is_loading:
            if self.view.main_window.get_sensitive():
                self.view.main_window.set_sensitive(False)
            GLib.idle_add(self.view.on_loading)
            self.is_loading = True

        # Muestra el diálogo de carga
        def thread_function():
            try:
                # Ejecuta la función en segundo plano y captura el resultado
                result = func(*args)
                if callback:
                    GLib.idle_add(callback, result)  # Llama al callback con el resultado
            except Exception as e:
                # Manejo de otras excepciones
                GLib.idle_add(self.on_service_failure, str(e))
            finally:
                # Asegura que el spinner se detenga después de la operación
                GLib.idle_add(self.view.stop_loading)
                self.is_loading = False

        # Inicia el hilo en segundo plano
        threading.Thread(target=thread_function).start()

    # Función para manejar un fallo de servicio
    def on_service_failure(self, message="Ha ocurrido un fallo de servicio."):
        """Maneja un fallo de servicio"""
        if self.view.main_window.get_sensitive():
            self.view.main_window.set_sensitive(False)

        GLib.idle_add(self.view.on_service_failure, message)

    # Función para reintentar la última operación.
    def retry_last_operation(self):
        """Reintenta la última operación"""

        GLib.idle_add(self.view.service_failure_dialog.destroy)
        GLib.idle_add(self.view.main_window.set_sensitive, True)
        self.last_operation()

    # Función para cambiar la vista actual
    def set_view(self, view):
        """Cambia la vista del presenter"""
        self.view = view

    # Función para construir el menú de pantallas
    def build_menu(self):
        self.view.build_menu(self.view_manager.views)

    # Función para cambiar de vista
    def on_change_view(self, view_name):
        self.initialize_view(self.view_manager.get_view(view_name))

    # Función para actualizar la paginación en función de la página actual
    def update_pagination(self):
        if self.current_page == 0:
            self.view.previous_button.set_sensitive(False)
        else:
            self.view.previous_button.set_sensitive(True)
        if not self.exist_more_items:
            self.view.next_button.set_sensitive(False)
        else:
            self.view.next_button.set_sensitive(True)
        self.view.page_label.set_text(f"Página {self.current_page + 1}")


    def restart_page(self):
        self.current_page = 0


    # --------------------------------- CAMISETAS ---------------------------------

    # Función para inicializar las camisetas
    def load_shirts(self):
        self.last_operation = partial(self.load_shirts)

        keywords = self.view.search_entry.get_text().strip()

        self.run_in_thread(
            self.shirt_model.search_shirts,
            self.on_shirts_loaded,
            keywords,  # keywords
            self.view.filter_shirt_type_id,  # shirt_type_id
            self.view.filter_collaborator_id,  # investor_id
            self.view.filter_shirt_size,  # shirt_size
            self.view.filter_is_responsible,  # is_responsible
            self.current_page # page
        )

    # Función para cuando las camisetas están cargadas
    def on_shirts_loaded(self, result):
        self.shirts, self.exist_more_items = result
        self.view.show_shirts(self.shirts)
        self.update_pagination()

    # Función para cambiar a la página anterior
    def on_shirt_previous_page(self):
        if self.current_page > 0:
            self.current_page -= 1
            self.load_shirts()

    # Función para cambiar a la página siguiente
    def on_shirt_next_page(self):
        if self.exist_more_items:
            self.current_page += 1
            self.load_shirts()

    # Función para aplicar el efecto de camiseta comprada en la vista
    def apply_shirts_bought_styles(self, shirt, row):
        if shirt.bought:
            row.add_css_class("completed-row")

    # Función para buscar camisetas
    def on_shirt_search_activate(self):
        self.load_shirts()

    # Función para cuando se selecciona una camiseta
    def on_shirt_selected(self, shirt):
        self.view.clear_shirt_details_box()

        self.last_operation = partial(self.on_shirt_selected, shirt)

        self.run_in_thread(
            self.shirt_model.get_shirt,
            self.on_shirt_loaded,
            shirt.id
        )

    # Función para cuando se ha cargado la camiseta seleccionada
    def on_shirt_loaded(self, shirt):
        self.shirt = shirt
        self.view.shirt = shirt

        if shirt.bought:
            self.view.buy_button_label = "Deshacer compra"
            self.view.buy_tooltip_label = "Deshacer compra de camiseta"
            self.view.sale_price_label = f"Vendida por: {shirt.sale_price} €"
        else:
            self.view.buy_button_label = "Comprar"
            self.view.buy_tooltip_label = "Comprar camiseta"

        # INICIALIZAR contadores
        self.pending_calls = 3  # is_responsible + friends number + friends bought
        if not shirt.bought:
            self.pending_calls += 1  # current price si aplica
            self.set_current_price()

        self.model_is_responsible()
        self.set_customer_convinced_friends_number()
        self.set_customer_convinced_friends_bought()

    def on_shirt_type_link_clicked(self, shirt_type_id):
        self.on_change_view("Peñas")
        self.run_in_thread(
            self.shirt_model.get_shirt_type,
            self.on_shirt_type_loaded,
            shirt_type_id
        )


    def on_filter_clicked(self):
        self.shirt_form_option = "filter"
        self.load_form_params()

    def on_add_shirt_clicked(self):
        self.shirt_form_option = "add"
        self.load_form_params()
        
    def on_update_shirt_clicked(self):
        self.shirt_form_option = "update"
        self.load_form_params()

    def add_shirt(self, instagram, purchase_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, name, surname, phone_number):
        self.last_operation = partial(self.add_shirt, instagram, purchase_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, name, surname, phone_number)

        self.run_in_thread(
            self.shirt_model.add_shirt,
            self.on_shirt_updated,
            instagram,
            purchase_price,
            shirt_type_id,
            investor_id,
            size,
            convincing_friend_id,
            is_responsible,
            name,
            surname,
            phone_number
        )
        
    def update_shirt(self, instagram, purchase_price, sale_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, responsible_id, name, surname, phone_number):
        self.last_operation = partial(self.update_shirt, instagram, purchase_price, sale_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, responsible_id, name, surname, phone_number)
        
        self.run_in_thread(
            self.shirt_model.update_shirt,
            self.on_shirt_updated,
            self.shirt.id,
            instagram,
            purchase_price,
            sale_price,
            shirt_type_id,
            investor_id,
            size,
            convincing_friend_id,
            is_responsible,
            responsible_id,
            name,
            surname,
            phone_number
        )

    def on_shirt_updated(self, shirt):
        self.load_list(self.view)
        self.view.clear_shirt_details_box()
        self.on_shirt_loaded(shirt)

    def check_convinced_friend_by_instagram(self, instagram):

        if self.view.convinced_friend_entry.get_text().strip() == "":
            self.view.on_shirt_form_validated(self.shirt_form_option)

        else:
            self.last_operation = partial(self.load_form_params)

            self.run_in_thread(
                self.shirt_model.get_shirt_by_instagram,
                self.on_convinced_friend_checked,
                instagram
            )

    def on_convinced_friend_checked(self, response):
        valid, convincing_friend = response

        if valid:
            self.view.convincing_friend = convincing_friend
            self.view.on_shirt_form_validated(self.shirt_form_option)

        else:
            self.view.shirt_form_error_label.set_markup(f'<span foreground="red">El amigo del cliente no está registrado</span>')
            self.view.shirt_form_error_label.set_visible(True)


    def load_form_params(self):
        self.last_operation = partial(self.load_form_params)

        self.run_in_thread(
            self.shirt_model.get_all_shirt_types,
            self.on_form_params_loaded,
        )

    def on_form_params_loaded(self, shirt_types):

        if self.shirt_form_option == "filter":
            self.shirt_filter_shirt_types = shirt_types

        elif self.shirt_form_option == "add":
            self.update_shirt_types = shirt_types

        elif self.shirt_form_option == "update":
            self.update_shirt_types = shirt_types

        self.load_form_collaborators()

    def load_form_collaborators(self):
        self.last_operation = partial(self.load_form_params)

        self.run_in_thread(
            self.collaborator_model.get_all_collaborators,
            self.on_form_collaborators_loaded
        )

    def on_form_collaborators_loaded(self, collaborators):

        if self.shirt_form_option == "filter":
            self.shirt_filter_collaborators = collaborators
            self.view.build_filter_dialog(self.shirt_filter_shirt_types, self.shirt_filter_collaborators, self.shirt_form_sizes)

        elif self.shirt_form_option == "add":
            self.update_shirt_collaborators = collaborators
            self.view.build_shirt_form(self.update_shirt_types, self.update_shirt_collaborators, self.shirt_form_sizes)

        elif self.shirt_form_option == "update":
            self.update_shirt_collaborators = collaborators
            self.view.build_shirt_form(self.update_shirt_types, self.update_shirt_collaborators, self.shirt_form_sizes, self.shirt)


    # Función si pulsan en el amigo convencedor de una camiseta
    def on_convincing_friend_clicked(self, customer_id):
        self.last_operation = partial(self.on_convincing_friend_clicked, customer_id)
        self.run_in_thread(
            self.shirt_model.get_shirt_by_customer_id,
            self.on_shirt_loaded,
            customer_id
        )

    # Función que usa el servicio para saber si el cliente de una camiseta es el responsable de su peña
    def model_is_responsible(self):
        self.last_operation = partial(self.on_shirt_selected)

        self.run_in_thread(
            self.shirt_model.is_responsible,
            self.set_responsible_data,
            self.shirt.customer_id
        )

    # Función para obtener los datos del responsable
    def set_responsible_data(self, is_responsible):
        self.last_operation = partial(self.on_shirt_selected)

        if not is_responsible:
            self.shirt_responsible = None
            self.view.shirt_responsible = None

        self.check_shirt_pending_calls()

        if is_responsible:
            self.pending_calls += 1
            self.run_in_thread(
                self.shirt_model.get_responsible_by_customer_id,
                self.on_responsible_data_setted,
                self.shirt.customer_id
            )

    # Función para modificar los datos del responsable de la vista
    def on_responsible_data_setted(self, responsible):
        self.shirt_responsible = responsible
        self.view.shirt_responsible = responsible
        self.check_shirt_pending_calls()
        self.set_customer_convinced_friends_number()

    # Función para obtener el número de amigos convencidos de una camiseta
    def set_customer_convinced_friends_number(self):
        self.last_operation = partial(self.on_shirt_selected)

        self.run_in_thread(
            self.shirt_model.convinced_friends_number,
            self.on_customer_convinced_friends_number_setted,
            self.shirt.id
        )

    # Función para modificar el número de amigos convencidos de la vista
    def on_customer_convinced_friends_number_setted(self, number):
        self.check_shirt_pending_calls()
        self.view.customer_convinced_friends_number = number
        self.set_customer_convinced_friends_bought()

    # Función para obtener el número de amigos convencidos que han pagado la camiseta
    def set_customer_convinced_friends_bought(self):
        self.last_operation = partial(self.on_shirt_selected)

        self.run_in_thread(
            self.shirt_model.convinced_friends_paid,
            self.on_customer_convinced_friends_bought_setted,
            self.shirt.id
        )

    # Función para modificar el número de amigos convencidos que han pagado de la vista
    def on_customer_convinced_friends_bought_setted(self, number):
        self.view.customer_convinced_friends_bought = number
        self.check_shirt_pending_calls()
        
    def on_convinced_friend_clicked(self, convinced_friend):
        self.last_operation = partial(self.on_convinced_friend_clicked, convinced_friend)
        self.on_shirt_loaded(convinced_friend)

    # Función para obtener el precio actual de la camiseta
    def set_current_price(self):
        self.last_operation = partial(self.on_shirt_selected)

        self.run_in_thread(
            self.shirt_model.get_current_price,
            self.on_current_price_got,
            self.shirt.id
        )

    # Función para modificar el precio actual de la vista
    def on_current_price_got(self, price):
        self.view.sale_price_label = f"Precio actual: {price} €"
        self.shirt_sale_price = price
        self.view.current_price = price
        self.check_shirt_pending_calls()

    # Función por si cambia la entrada de la etiqueta del texto
    def on_big_decimal_changed(self, text, attribute, error_label, ok_button):
        try:
            decimal = Decimal(text)

            if 0 <= decimal <= Decimal("999999999.99"):
                # Es válido → limpia mensaje de error
                error_label.set_markup("")
                ok_button.set_sensitive(True)
                error_label.set_visible(False)

            else:
                # No válido → muestra mensaje rojo
                error_label.set_markup(
                    f"<span foreground='red'>{attribute} no válido: debe estar entre 0 y 999999999.99</span>"
                )
                ok_button.set_sensitive(False)
                error_label.set_visible(True)

        except Exception:
            error_label.set_markup(
                f"<span foreground='red'>{attribute} no válido: debe ser numérico</span>"
            )

            ok_button.set_sensitive(False)
            error_label.set_visible(True)

    # Función por si cambia la entrada de la etiqueta del texto
    def on_int_changed(self, text, attribute, error_label, ok_button):
        try:
            integer = int(text)

            if 0 <= integer <= 1000000000:
                # Es válido → limpia mensaje de error
                error_label.set_markup("")
                ok_button.set_sensitive(True)
                error_label.set_visible(False)

            else:
                # No válido → muestra mensaje rojo
                error_label.set_markup(
                    f"<span foreground='red'>{attribute} no válido: debe estar entre 0 y 1000000000</span>"
                )
                ok_button.set_sensitive(False)
                error_label.set_visible(True)

        except Exception:
            error_label.set_markup(
                f"<span foreground='red'>{attribute} no válido: debe ser numérico</span>"
            )

            ok_button.set_sensitive(False)
            error_label.set_visible(True)

    # Función por si cambia la entrada de la etiqueta del texto
    def on_string_changed(self, text, max_chars, attribute, error_label, ok_button):

        if len(text.get_text()) <= max_chars:
                # Es válido → limpia mensaje de error
                error_label.set_markup("")
                ok_button.set_sensitive(True)
                error_label.set_visible(False)

        else:
            # No válido → muestra mensaje rojo
            error_label.set_markup(
                f"<span foreground='red'>{attribute} no válido: límite de caracteres superado</span>"
            )
            ok_button.set_sensitive(False)
            error_label.set_visible(True)


    def on_buy_entry_changed(self, text):
        try:
            price = Decimal(text)

            if 0 <= price <= Decimal("999999999.99"):
                # Es válido → limpia mensaje de error
                self.view.buy_entry_error_label.set_markup("")

                self.view.buy_ok_button.set_sensitive(True)

                self.shirt_sale_price = price
                self.view.current_price = price
            else:
                # No válido → muestra mensaje rojo
                self.view.buy_entry_error_label.set_markup(
                    "<span foreground='red'>Precio no válido: debe estar entre 0 y 999999999.99</span>"
                )

                self.view.buy_ok_button.set_sensitive(False)

        except Exception:
            self.view.buy_entry_error_label.set_markup(
                "<span foreground='red'>Precio no válido: debe ser numérico</span>"
            )

            self.view.buy_ok_button.set_sensitive(False)

    def on_shirt_convinced_friends_clicked(self, shirt):
        self.last_operation = partial(self.on_shirt_convinced_friends_clicked, shirt)
        self.run_in_thread(
            self.shirt_model.convinced_friends,
            self.on_shirt_convinced_friends_loaded,
            self.shirt.id
        )

    def on_shirt_convinced_friends_loaded(self, convinced_friends):
        self.last_operation = partial(self.on_shirt_convinced_friends_clicked, self.shirt)
        self.convinced_friends = convinced_friends
        self.run_in_thread(
            self.shirt_model.get_shirt_type,
            self.build_convinced_friends_view,
            self.shirt.shirt_type_id
        )

    def build_convinced_friends_view(self, shirt_type):
        self.view.build_convinced_friends(shirt_type.free_shirt_people, self.convinced_friends)

    # Función para comprar una camiseta mediante el servicio
    def on_buy_shirt(self, shirt, current_price):
        self.last_operation = partial(self.on_buy_shirt, shirt, current_price)

        if current_price != self.shirt_sale_price:
            self.on_service_failure("Ha ocurrido un error con la compra")

        self.run_in_thread(
            self.shirt_model.buy,
            self.on_shirt_updated,
            self.shirt.id,
            self.shirt_sale_price
        )



    # Función para deshacer la compra de una camiseta actualizándola mediante el servicio
    def on_undo_shirt_purchase(self, shirt):
        self.shirt = shirt
        self.last_operation = partial(self.on_undo_shirt_purchase, self.shirt)
        self.run_in_thread(
            self.shirt_model.update_shirt,
            self.on_shirt_updated,
            self.shirt.id,
            self.shirt.customer_instagram,
            self.shirt.purchase_price,
            None,
            self.shirt.shirt_type_id,
            self.shirt.investor_id,
            self.shirt.size,
            self.shirt.convincing_friend_id,
            self.shirt_responsible is not None,
            self.shirt_responsible.id if self.shirt_responsible else None,
            self.shirt_responsible.name if self.shirt_responsible else None,
            self.shirt_responsible.surname if self.shirt_responsible else None,
            self.shirt_responsible.phone_number if self.shirt_responsible else None,
        )

    # Función para aplicar el estilo del botón de comprar/deshacer compra
    def apply_buy_button_styles(self, button):
        if self.shirt.bought:
            self.view.buy_button.get_style_context().add_class("cancel-button")
        else:
            self.view.buy_button.get_style_context().add_class("confirm-button")

    # Función para borrar una camiseta mediante el servicio
    def on_delete_shirt(self, shirt):
        self.last_operation = partial(self.on_delete_shirt, shirt)

        self.run_in_thread(
            self.shirt_model.delete_shirt,
            self.on_shirt_removed,
            shirt.id
        )

    # Función una vez se ha borrado una camiseta
    def on_shirt_removed(self, result):
        self.shirt = None
        self.shirt_responsible = None
        self.pending_calls = 0
        self.load_shirts()
        self.view.clear_shirt_details_box()

    # Función auxiliar para coordinar las llamadas asíncronas
    def check_shirt_pending_calls(self):
        self.pending_calls -= 1
        if self.pending_calls == 0:
            self.view.show_shirt_details(self.shirt, self.shirt_responsible)


    # --------------------------------- PEÑAS ---------------------------------

    # Función para inicializar las peñas
    def load_shirt_types(self):
        self.last_operation = partial(self.load_shirt_types)

        name = self.view.search_entry.get_text().strip()

        self.run_in_thread(
            self.shirt_model.get_shirt_types,
            self.on_shirt_types_loaded,
            name,  # keywords
            self.current_page  # page
        )

    # Función una vez se han cargado las peñas
    def on_shirt_types_loaded(self, result):
        self.shirt_types, self.exist_more_items = result
        self.view.show_shirt_types(self.shirt_types)
        self.update_pagination()

    # Función para cambiar a la página anterior
    def on_shirt_type_previous_page(self):
        if self.current_page > 0:
            self.current_page -= 1
            self.load_shirt_types()

    # Función para cambiar a la página siguiente
    def on_shirt_type_next_page(self):
        if self.exist_more_items:
            self.current_page += 1
            self.load_shirt_types()

    # Función para buscar peñas
    def on_shirt_type_search_activate(self):
        self.load_shirt_types()

    # Función para cargar los datos de una peña
    def on_shirt_type_selected(self, shirt_type):
        self.last_operation = partial(self.on_shirt_type_selected, shirt_type)

        self.run_in_thread(
            self.shirt_model.get_shirt_type,
            self.on_shirt_type_loaded,
            shirt_type.id
        )

    # Función para modificar los datos de una peña en la vista
    def on_shirt_type_loaded(self, shirt_type):
        self.shirt_type = shirt_type

        self.pending_calls = 3

        if self.shirt_type.responsible_id is not None:
            self.pending_calls += 1
            self.load_shirt_type_responsible()

        self.load_shirt_type_customers_number()

        self.load_shirt_type_revenue()

        self.exists_raffle_by_shirt_type_id()


    # Función para cargar el responsable de la peña
    def load_shirt_type_responsible(self):
        self.last_operation = partial(self.on_shirt_type_selected)

        self.run_in_thread(
            self.shirt_model.get_responsible,
            self.on_shirt_type_responsible_loaded,
            self.shirt_type.responsible_id
        )

    # Función para modificar el responsable de la peña en la vista
    def on_shirt_type_responsible_loaded(self, responsible):
        self.shirt_type_responsible = responsible
        self.check_shirt_type_pending_calls()

    # Función para cargar el número de clientes de la peña
    def load_shirt_type_customers_number(self):
        self.last_operation = partial(self.on_shirt_type_selected)

        self.run_in_thread(
            self.shirt_model.customers_number,
            self.on_shirt_type_customers_number_loaded,
            self.shirt_type.id
        )

    # Función para modificar el número de clientes de la peña en la vista
    def on_shirt_type_customers_number_loaded(self, customers_number):
        self.view.customers_number = customers_number
        self.check_shirt_type_pending_calls()

    # Función para cargar el beneficio de la peña
    def load_shirt_type_revenue(self):
        self.last_operation = partial(self.on_shirt_type_selected)

        self.run_in_thread(
            self.shirt_model.shirt_type_revenue,
            self.on_shirt_type_revenue_loaded,
            self.shirt_type.id
        )

    # Función para modificar el beneficio de la peña en la vista
    def on_shirt_type_revenue_loaded(self, revenue):
        self.view.shirt_type_revenue = revenue
        self.check_shirt_type_pending_calls()

    def exists_raffle_by_shirt_type_id(self):
        self.last_operation = partial(self.on_shirt_type_selected)

        self.run_in_thread(
            self.raffle_model.exists_by_shirt_type_id,
            self.on_raffle_exists_loaded,
            self.shirt_type.id
        )

    def on_raffle_exists_loaded(self, exists):
        self.view.exists_raffle = exists
        self.check_shirt_type_pending_calls()


    def on_edit_raffle_clicked(self):
        self.raffle_form_option = "update"
        self.view.build_raffle_form(self.raffle)


    def on_raffle_clicked(self, exists):
        if exists:
            self.on_raffle_selected()
        else:
            self.raffle_form_option = "add"
            self.view.build_raffle_form()

    def on_raffle_form_completed(self):
        self.view.on_raffle_form_validated(self.raffle_form_option)

    def on_raffle_selected(self):
        self.last_operation = partial(self.on_raffle_selected)

        self.run_in_thread(
            self.raffle_model.get_raffle_by_shirt_type_id,
            self.on_raffle_loaded,
            self.shirt_type.id
        )

    def on_raffle_loaded(self, raffle):
        self.raffle = raffle
        self.view.raffle = raffle
        self.view.build_raffle_view()

    def add_raffle(self, participation_price, description):
        self.last_operation = partial(self.add_raffle, participation_price, description)
        self.run_in_thread(
            self.raffle_model.add_raffle,
            self.on_raffle_added,
            participation_price,
            description,
            self.shirt_type.id
        )

    def update_raffle(self, participation_price, description):
        self.last_operation = partial(self.update_raffle, participation_price, description)
        self.run_in_thread(
            self.raffle_model.update_raffle,
            self.on_raffle_updated,
            self.raffle.id,
            participation_price,
            description,
            self.raffle.shirt_type_id
        )

    def on_raffle_added(self, raffle):
        self.on_shirt_type_updated(self.shirt_type)

    def on_raffle_updated(self, raffle):
        self.view.see_raffle_dialog.destroy()
        self.on_raffle_selected()

    def delete_raffle(self, raffle):
        self.last_operation = partial(self.delete_raffle, raffle)
        self.run_in_thread(
            self.raffle_model.delete_raffle,
            self.on_raffle_updated,
            raffle.id,
        )

    def on_add_level_clicked(self):
        self.level_form_option = "add"
        self.view.build_level_form()

    def on_update_level_clicked(self):
        self.level_form_option = "update"
        self.view.build_level_form(self.level)

    def on_level_form_completed(self):
        self.view.on_level_form_validated(self.level_form_option)

    def add_level(self, description, price, necessary_participants):
        self.last_operation = partial(self.add_level, description, price, necessary_participants)
        self.run_in_thread(
            self.raffle_model.add_level,
            self.on_level_updated,
            description,
            price,
            necessary_participants,
            self.raffle.id
        )

    def update_level(self, description, price, necessary_participants):
        self.last_operation = partial(self.update_level, description, price, necessary_participants)
        self.run_in_thread(
            self.raffle_model.update_level,
            self.on_level_updated,
            self.level.id,
            description,
            price,
            necessary_participants,
        )

    def delete_level(self, level):
        self.last_operation = partial(self.delete_level, level)

        self.run_in_thread(
            self.raffle_model.delete_level,
            self.on_level_updated,
            level.id
        )

    def can_play(self, level):
        self.last_operation = partial(self.can_play, level)
        self.run_in_thread(
            self.raffle_model.level_raised,
            self.level_raised,
            level.id
        )

    def level_raised(self, boolean):
        self.view.level_raised = boolean
        self.load_level_participants()

    def load_level_participants(self):
        self.last_operation = partial(self.load_level_participants)

        self.run_in_thread(
            self.raffle_model.get_raffle_participants_number,
            self.on_raffle_participants_loaded,
            self.raffle.id
        )

    def on_raffle_participants_loaded(self, number):
        self.view.level_participants = number
        self.view.build_level_view()


    def on_level_updated(self, level):
        if self.level_form_option == "update":
            self.view.see_level_dialog.destroy()

        self.view.see_raffle_dialog.destroy()
        self.on_raffle_selected()

    def on_level_clicked(self, level, num_level):
        self.level = level
        self.view.level = level
        self.view.num_level = num_level
        self.can_play(level)

    def play_level(self, level):
        self.last_operation = partial(self.play_level, level)
        self.run_in_thread(
            self.raffle_model.play_level,
            self.on_level_played,
            self.level.id,
        )

    def on_level_played(self, winner):
        self.view.see_level_dialog.destroy()
        self.view.see_raffle_dialog.destroy()
        self.view.on_winner_response(winner.winner_instagram)

    def on_winner_link_clicked(self, level):

        self.view.see_level_dialog.destroy()
        self.view.see_raffle_dialog.destroy()
        self.on_change_view("Camisetas")

        self.last_operation = partial(self.on_winner_link_clicked, level)
        self.run_in_thread(
            self.shirt_model.get_shirt_by_customer_id,
            self.on_shirt_selected,
            level.winner_id
        )


    def on_customers_number_clicked(self, shirt_type):
        self.shirts_view.filter_shirt_type_id = shirt_type.id
        self.on_change_view("Camisetas")

    def on_shirt_type_responsible_clicked(self, responsible):
        self.last_operation = partial(self.on_shirt_type_responsible_clicked, responsible)
        self.run_in_thread(
            self.shirt_model.get_shirt_by_customer_id,
            self.on_shirt_type_responsible_clicked_and_loaded,
            responsible.customer_id
        )

    def on_shirt_type_responsible_clicked_and_loaded(self, shirt):
        self.on_change_view("Camisetas")
        self.on_shirt_selected(shirt)

    def on_add_shirt_type_clicked(self):
        self.shirt_type_form_option = "add"
        self.on_shirt_type_form_loaded()

    def on_update_shirt_type_clicked(self):
        self.shirt_type_form_option = "update"
        self.on_shirt_type_form_loaded()

    def add_shirt_type(self, name, image, base_sales_price, description, free_shirt_people):
        self.last_operation = partial(self.add_shirt_type, name, image, base_sales_price, description, free_shirt_people)

        self.run_in_thread(
            self.shirt_model.add_shirt_type,
            self.on_shirt_type_updated,
            name,
            image,
            base_sales_price,
            description,
            free_shirt_people
        )

    def update_shirt_type(self, name, image, base_sales_price, description, free_shirt_people):
        self.last_operation = partial(self.update_shirt_type,name, image, base_sales_price, description, free_shirt_people)

        self.run_in_thread(
            self.shirt_model.update_shirt_type,
            self.on_shirt_type_updated,
            self.shirt_type.id,
            name,
            image,
            base_sales_price,
            description,
            free_shirt_people,
            self.shirt_type.responsible_id
        )

    def on_shirt_type_updated(self, shirt_type):
        self.load_list(self.view)
        self.view.clear_shirt_type_details_box()
        self.on_shirt_type_loaded(shirt_type)

    def on_shirt_type_form_loaded(self):

        if self.shirt_type_form_option == "add":
            self.view.build_shirt_type_form()

        elif self.shirt_type_form_option == "update":
            self.view.build_shirt_type_form(self.shirt_type)

    def on_shirt_type_form_completed(self):
        self.view.on_shirt_type_form_validated(self.shirt_type_form_option)


    def on_delete_shirt_type(self, shirt_type):
        self.last_operation = partial(self.on_delete_shirt_type, shirt_type)

        self.run_in_thread(
            self.shirt_model.delete_shirt_type,
            self.on_shirt_type_removed,
            self.shirt_type.id
        )

    def on_shirt_type_removed(self, result):
        if self.shirts_view.shirt_type_combo and self.shirt_type.id == self.shirts_view.shirt_type_combo.get_active_id():
            self.shirts_view.shirt_type_combo.set_active_id("")
            self.shirts_view.filter_shirt_type_id = None

        self.shirt_type = None
        self.shirt_type_responsible = None
        self.pending_calls = 0
        self.load_shirt_types()
        self.view.clear_shirt_type_details_box()

    # Función auxiliar para coordinar las llamadas asíncronas
    def check_shirt_type_pending_calls(self):
        self.pending_calls -= 1
        if self.pending_calls == 0:
            self.view.show_shirt_type_details(self.shirt_type, self.shirt_type_responsible)


    # --------------------------------- INVERSORES ---------------------------------

    # Función para cargar el beneficio total
    def load_total_profit(self):
        self.last_operation = partial(self.load_total_profit)

        self.run_in_thread(
            self.shirt_model.get_total_profit,
            self.on_total_profit_loaded,
        )

    # Función para modificar el beneficio total en la vista
    def on_total_profit_loaded(self, profit):
        self.view.total_profit_label_price.set_markup(f"<b>{profit} €</b>")
        self.load_collaborators()

    # Función para inicializar los colaboradores
    def load_collaborators(self):
        self.last_operation = partial(self.load_collaborators)

        name = self.view.search_entry.get_text().strip()

        self.run_in_thread(
            self.collaborator_model.get_collaborators,
            self.on_collaborators_loaded,
            name,  # keywords
            self.current_page  # page
        )

    # Función para actualizar los colaboradores en la vista
    def on_collaborators_loaded(self, result):
        self.collaborators, self.exist_more_items = result
        self.view.show_collaborators(self.collaborators)
        self.update_pagination()


    # Función para cambiar a la página anterior
    def on_collaborator_previous_page(self):
        if self.current_page > 0:
            self.current_page -= 1
            self.load_collaborators()

    # Función para cambiar a la página siguiente
    def on_collaborator_next_page(self):
        if self.exist_more_items:
            self.current_page += 1
            self.load_collaborators()

    # Función para buscar colaboradores
    def on_collaborator_search_activate(self):
        self.load_collaborators()

    # Función para cargar los datos de un colaborador
    def on_collaborator_selected(self, collaborator):
        self.last_operation = partial(self.on_collaborator_selected, collaborator)

        self.run_in_thread(
            self.collaborator_model.get_collaborator,
            self.on_collaborator_loaded,
            collaborator.id
        )

    # Función para modificar los datos de un colaborador en la vista
    def on_collaborator_loaded(self, collaborator):
        self.collaborator = collaborator

        self.pending_calls = 4

        self.load_shirts_number()

        self.load_investment()

        self.load_returned_investment()

        self.load_profit()


    # Función para cargar el número de camisetas de un colaborador
    def load_shirts_number(self):
        self.last_operation = partial(self.on_collaborator_selected)

        self.run_in_thread(
            self.collaborator_model.get_number_of_bought_shirts,
            self.on_collaborator_shirts_number_loaded,
            self.collaborator.id
        )

    # Función para modificar el número de camisetas de un colaborador en la vista
    def on_collaborator_shirts_number_loaded(self, number):
        self.view.shirts_number = number
        self.check_collaborator_pending_calls()

    # Función para cargar la inversión de un colaborador
    def load_investment(self):
        self.last_operation = partial(self.on_collaborator_selected)

        self.run_in_thread(
            self.collaborator_model.get_investment,
            self.on_collaborator_investment_loaded,
            self.collaborator.id
        )

    # Función para modificar la inversión de un colaborador en la vista
    def on_collaborator_investment_loaded(self, investment):
        self.view.investment = investment
        self.check_collaborator_pending_calls()

    # Función para cargar el retorno de inversión de un colaborador
    def load_returned_investment(self):
        self.last_operation = partial(self.on_collaborator_selected)

        self.run_in_thread(
            self.collaborator_model.get_returned_investment,
            self.on_collaborator_returned_investment_loaded,
            self.collaborator.id
        )

    # Función para modificar el retorno de inversión de un colaborador en la vista
    def on_collaborator_returned_investment_loaded(self, returned_investment):
        self.view.returned_investment = returned_investment
        self.check_collaborator_pending_calls()


    # Función para cargar el beneficio del colaborador
    def load_profit(self):
        self.last_operation = partial(self.on_collaborator_selected)

        self.run_in_thread(
            self.collaborator_model.get_profit,
            self.on_collaborator_profit_loaded,
            self.collaborator.id
        )

    # Función para modificar el beneficio del colaborador en la vista
    def on_collaborator_profit_loaded(self, profit):
        self.view.profit = profit
        self.check_collaborator_pending_calls()


    def on_delete_collaborator(self, collaborator):
        self.last_operation = partial(self.on_delete_collaborator, collaborator)

        self.run_in_thread(
            self.collaborator_model.delete_collaborator,
            self.on_collaborator_removed,
            self.collaborator.id
        )

    def on_collaborator_removed(self, result):

        if self.shirts_view.collaborator_combo and self.collaborator.id == self.shirts_view.collaborator_combo.get_active_id():
            self.shirts_view.collaborator_combo.set_active_id("")
            self.shirts_view.filter_collaborator_id = None

        self.collaborator = None
        self.pending_calls = 0
        self.load_collaborators()
        self.view.clear_collaborator_details_box()

    def on_add_collaborator_clicked(self):
        self.collaborator_form_option = "add"
        self.view.build_collaborator_form()

    def on_update_collaborator_clicked(self):
        self.collaborator_form_option = "update"
        self.view.build_collaborator_form(self.collaborator)

    def add_collaborator(self, name, percentage):
        self.last_operation = partial(self.add_collaborator, name, percentage)

        self.run_in_thread(
            self.collaborator_model.add_collaborator,
            self.on_collaborator_updated,
            name,
            percentage
        )

    def update_collaborator(self, name, percentage):
        self.last_operation = partial(self.update_collaborator, name, percentage)

        self.run_in_thread(
            self.collaborator_model.update_collaborator,
            self.on_collaborator_updated,
            self.collaborator.id,
            name,
            percentage
        )

    def on_collaborator_updated(self, collaborator):
        self.initialize_view(self.view)
        self.view.clear_collaborator_details_box()
        self.on_collaborator_loaded(collaborator)

    def check_collaborator_percentage(self, percentage):

        if self.view.percentage_entry.get_text().strip() == "":
            self.view.collaborator_form_error_label.set_markup(
                f'<span foreground="red">El porcentaje de beneficio es obligatorio</span>')
            self.view.collaborator_form_error_label.set_visible(True)

        else:
            self.last_operation = partial(self.check_collaborator_percentage, percentage)

            self.run_in_thread(
                self.collaborator_model.get_valid_percentage,
                self.on_percentage_checked,
                self.collaborator.id if self.collaborator_form_option == "update" else None,
                percentage
            )

    def on_percentage_checked(self, is_valid):
        if is_valid:
            self.view.on_collaborator_form_validated(self.collaborator_form_option)

        else:
            self.view.collaborator_form_error_label.set_markup(
                f'<span foreground="red">El porcentaje no es válido porque la suma sobrepasa el 100%</span>')
            self.view.collaborator_form_error_label.set_visible(True)


    # Función auxiliar para coordinar las llamadas asíncronas
    def check_collaborator_pending_calls(self):
        self.pending_calls -= 1
        if self.pending_calls == 0:
            self.view.show_collaborator_details(self.collaborator)

