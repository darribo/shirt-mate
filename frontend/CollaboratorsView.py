from decimal import Decimal, ROUND_HALF_UP

import gi
from gi.overrides.Gdk import Gdk

gi.require_version('Gtk', '4.0')  # establecer versión
from gi.repository import Gtk, Gio, GObject

WINDOW_PADDING = 12

class CollaboratorsView:
    def __init__(self, main_window: Gtk.Window):

        self.main_window = main_window

        self.main_window.set_default_size(1000, 600)

        self.total_profit = None
        self.collaborator = None
        self.shirts_number = None
        self.investment = None
        self.returned_investment = None
        self.profit = None

        # Contenedor principal
        self.main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=20)
        self.main_box.set_margin_top(WINDOW_PADDING)
        self.main_box.set_margin_bottom(WINDOW_PADDING)
        self.main_box.set_margin_start(WINDOW_PADDING)
        self.main_box.set_margin_end(WINDOW_PADDING)

        self.widget = self.main_box

        self.create_searchbar()

        # Panel dividido en 2
        self.paned = Gtk.Paned(orientation=Gtk.Orientation.HORIZONTAL)
        self.main_box.append(self.paned)

        self.left_box = self._initialize_left_panel()
        self.right_box = self._initialize_right_panel()

        # Añadir las cajas al Gtk.Paned
        self.paned.set_start_child(self.left_box)
        self.paned.set_end_child(self.right_box)
        self.paned.set_position(540)  # posición inicial de división en 600 píxeles para left_box y el resto para right_box

        self.load_dialog = None
        self.load_spinner = None

        self.apply_css()


    # Función para cambiar el presentador
    def set_presenter(self, presenter):
        """Asignar el presentador (handler) que manejará las interacciones"""
        self.presenter = presenter

    # Función para crear la barra de búsqueda
    def create_searchbar(self):

        searchbar_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=6)
        searchbar_box.add_css_class("toolbar")

        # Botón de menú hamburguesa para cambiar de vista
        self.menu_button = Gtk.MenuButton()
        self.menu_button.set_icon_name("open-menu-symbolic")  # Icono estándar de hamburguesa
        self.menu_button.set_tooltip_text("Menú")

        # Popover que contendrá la lista de vistas
        self.menu_popover = Gtk.Popover()
        self.menu_button.set_popover(self.menu_popover)

        self.menu_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=6)
        self.menu_box.set_margin_top(10)
        self.menu_box.set_margin_bottom(10)
        self.menu_box.set_margin_start(10)
        self.menu_box.set_margin_end(10)

        self.menu_popover.set_child(self.menu_box)

        searchbar_box.prepend(self.menu_button)  # Lo coloca al principio del box

        # Botón de recargar lista de inversores
        reload_button = Gtk.Button()
        reload_button.set_margin_start(0)
        reload_button.set_icon_name("view-refresh-symbolic")
        reload_button.set_tooltip_text("Recargar inversores")
        reload_button.connect("clicked", self.on_reload_clicked)
        searchbar_box.append(reload_button)

        # Separador vertical
        separator = Gtk.Separator(orientation=Gtk.Orientation.VERTICAL)
        searchbar_box.append(separator)

        # Campo de búsqueda
        self.search_entry = Gtk.SearchEntry()
        self.search_entry.set_placeholder_text("Buscar inversores")
        searchbar_box.append(self.search_entry)

        self.search_button = Gtk.Button(label="Buscar")
        self.search_button.set_tooltip_text("Buscar inversores")
        self.search_button.get_style_context().add_class("custom-search-button")
        self.search_button.connect("clicked", self.on_search_button_clicked)
        searchbar_box.append(self.search_button)

        # Añadir peña
        self.add_button = Gtk.Button()
        self.add_button.set_icon_name("list-add-symbolic")
        self.add_button.set_tooltip_text("Añadir inversor")
        self.add_button.get_style_context().add_class("custom-button")
        self.add_button.connect("clicked", self.on_add_collaborator_clicked)
        searchbar_box.append(self.add_button)

        total_profit_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=6)
        total_profit_box.add_css_class("total-profit-box")

        self.total_profit_label_price = Gtk.Label()

        total_profit_box.append(self.total_profit_label_price)

        searchbar_box.append(total_profit_box)

        self.main_box.append(searchbar_box)

    # Función para construir el menú de pantallas
    def build_menu(self, views):
        # Limpiar menú anterior
        child = self.menu_box.get_first_child()
        while child is not None:
            next_child = child.get_next_sibling()
            self.menu_box.remove(child)
            child = next_child

        for view in views:
            button = Gtk.Button(label=view)
            button.connect("clicked", self.on_menu_item_clicked, view)
            self.menu_box.append(button)

    # Función de manejar el cambio de pantalla
    def on_menu_item_clicked(self, button, view_name):
        self.presenter.on_change_view(view_name)

    # Función para recargar la lista de peñas
    def on_reload_clicked(self, widget):
        """Recarga la lista de inversores"""
        self.search_entry.set_text('')
        self.presenter.on_collaborator_search_activate()

    # Función por si cambia la búsqueda
    def on_search_button_clicked(self, widget):
        self.presenter.on_collaborator_search_activate()  # Llama al presenter para que procese la búsqueda

    # Función para inicializar el panel izquierdo
    def _initialize_left_panel(self):
        """Inicializa el panel izquierdo con la lista de inversores y los controles de paginación"""

        left_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        left_box.set_margin_top(10)
        left_box.set_margin_bottom(10)
        left_box.set_margin_start(10)
        left_box.set_margin_end(10)

        self.collaborators_list_box = Gtk.ListBox()
        self.collaborators_list_box.set_selection_mode(Gtk.SelectionMode.SINGLE)
        self.collaborators_list_box.connect("row-activated", self.on_collaborator_selected)  # Conectar el evento de clic

        scrolled_window = Gtk.ScrolledWindow()
        scrolled_window.set_policy(Gtk.PolicyType.NEVER, Gtk.PolicyType.AUTOMATIC)
        scrolled_window.set_child(self.collaborators_list_box)
        scrolled_window.set_vexpand(True)

        left_box.append(scrolled_window)

        # Controles de paginación
        pagination_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=6)
        pagination_box.set_halign(Gtk.Align.CENTER)

        self.previous_button = Gtk.Button()
        self.previous_button.set_icon_name("go-previous-symbolic")

        self.previous_button.set_tooltip_text("Página anterior")

        self.previous_button.connect("clicked", self.on_previous_page)
        pagination_box.append(self.previous_button)

        self.page_label = Gtk.Label()
        pagination_box.append(self.page_label)

        self.next_button = Gtk.Button()
        self.next_button.set_icon_name("go-next-symbolic")

        self.next_button.set_tooltip_text("Página siguiente")

        self.next_button.connect("clicked", self.on_next_page)
        pagination_box.append(self.next_button)

        left_box.append(pagination_box)

        return left_box

    # Función para cambiar a la página anterior
    def on_previous_page(self, widget):
        self.presenter.on_collaborator_previous_page()

    # Función para cambiar a la página siguiente
    def on_next_page(self, widget):
        self.presenter.on_collaborator_next_page()

    # Función para mostrar la lista de colaboradores en la interfaz
    def show_collaborators(self, collaborators):
        """Mostrar la lista de pacientes en la interfaz"""
        while True:
            row = self.collaborators_list_box.get_row_at_index(0)
            if row is None:
                break
            self.collaborators_list_box.remove(row)

        # Construir cada celda
        for collaborator in collaborators:
            row = Gtk.ListBoxRow()
            row.add_css_class('collaborators-list-row')

            collaborator_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
            row.set_child(collaborator_box)

            collaborator_avatar = Gtk.Image.new_from_file("icons/collaborator-avatar.png")
            collaborator_avatar.set_pixel_size(32)
            collaborator_box.append(collaborator_avatar)

            collaborator_attributes_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL)
            collaborator_box.append(collaborator_attributes_box)

            collaborator_name_label = Gtk.Label(label=f"<b>{collaborator.name}</b>")
            collaborator_name_label.set_use_markup(True)  # Hace que la interprete como letra negrita
            collaborator_name_label.set_halign(Gtk.Align.START)
            collaborator_attributes_box.append(collaborator_name_label)

            percentage_label = Gtk.Label(label=f"{collaborator.profit_percentage}% de beneficio")
            percentage_label.set_halign(Gtk.Align.START)
            collaborator_attributes_box.append(percentage_label)

            row.collaborator = collaborator
            self.collaborators_list_box.append(row)

    # Función por si se pulsa en una peña
    def on_collaborator_selected(self, list_box, row):
        self.presenter.on_collaborator_selected(row.collaborator)  # Todo: cambiar el self.collaborator

    # Función para añadir una peña
    def on_add_collaborator_clicked(self, widget):
        self.presenter.on_add_collaborator_clicked()

    # Función para inicializar el panel derecho
    def _initialize_right_panel(self):
        """Inicializa el panel derecho"""

        right_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        right_box.set_margin_top(10)
        right_box.set_margin_bottom(10)
        right_box.set_margin_start(10)
        right_box.set_margin_end(10)

        # Detalles de la camiseta
        self.collaborator_details_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.collaborator_details_box.set_vexpand(True)
        self.collaborator_details_box.set_halign(Gtk.Align.CENTER)
        right_box.append(self.collaborator_details_box)

        return right_box

    # Función para reiniciar la pantalla de detalles del inversor
    def clear_collaborator_details_box(self):
        while True:
            child = self.collaborator_details_box.get_first_child()
            if child is None:
                break
            self.collaborator_details_box.remove(child)

        self.collaborator = None

    # Función para mostrar los detalles de una peña
    def show_collaborator_details(self, collaborator):
        self.clear_collaborator_details_box()

        self.collaborator = collaborator

        header_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        header_separator.set_margin_top(10)
        header_separator.set_margin_bottom(10)

        self.collaborator_details_box.append(header_separator)

        self.content_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.content_box.set_vexpand(True)
        self.content_box.set_halign(Gtk.Align.CENTER)

        header_box = Gtk.CenterBox()
        header_box.set_hexpand(True)
        header_box.set_halign(Gtk.Align.FILL)

        # INSTAGRAM Y ACCIONES
        collaborator_name_label = Gtk.Label()
        collaborator_name_label.set_markup(f'<span size="20000"><b>{self.collaborator.name}</b></span>')
        collaborator_name_label.set_halign(Gtk.Align.CENTER)

        actions_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

        self.edit_button = Gtk.Button()
        self.edit_button.set_icon_name("document-edit-symbolic")
        self.edit_button.set_tooltip_text("Editar camiseta")
        self.edit_button.connect("clicked", self.on_update_collaborator_clicked)
        actions_box.append(self.edit_button)

        self.delete_button = Gtk.Button()
        self.delete_button.set_icon_name("edit-delete-symbolic")
        self.delete_button.set_tooltip_text("Eliminar camiseta")
        self.delete_button.connect("clicked", self.on_delete_collaborator_clicked)

        actions_box.append(self.delete_button)

        header_box.set_center_widget(collaborator_name_label)
        header_box.set_end_widget(actions_box)

        self.content_box.append(header_box)

        # Imagen de la peña
        percentage_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        percentage_box.set_halign(Gtk.Align.CENTER)
        percentage_box.set_margin_top(20)

        percentage_label = Gtk.Label()
        percentage_label.set_markup(f'<span size="12500"><b>Tiene un porcentaje del {self.collaborator.profit_percentage} del beneficio.</b></span>')
        percentage_label.set_halign(Gtk.Align.CENTER)

        percentage_box.append(percentage_label)

        self.content_box.append(percentage_box)

        body_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        body_separator.set_margin_top(10)
        body_separator.set_margin_bottom(10)
        self.content_box.append(body_separator)

        description_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        description_box.set_halign(Gtk.Align.CENTER)
        description_box.set_margin_top(20)
        description_label = Gtk.Label()

        returned = Decimal(self.returned_investment)
        invested = Decimal(self.investment)

        if invested > 0 and returned > 0:
            percentage = (returned / invested * Decimal(100)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        else:
            percentage = Decimal(0)

        description_label.set_markup(f'<span size="12500">Ha comprado <b>{self.shirts_number}</b> camisetas\n\nInvirtiendo <b>{invested}</b> €\n\nDe lo que ha recuperado <b>{returned} €</b>'
                                     f' (un <b>{percentage}%</b>)\n\n'
                                     f'A mayores, ha obtenido un beneficio de <b>{self.profit}</b> €</span>')
        description_label.set_halign(Gtk.Align.CENTER)
        description_label.set_justify(Gtk.Justification.CENTER)
        description_label.set_wrap(True)
        description_box.append(description_label)
        self.content_box.append(description_box)


        self.collaborator_details_box.append(self.content_box)

    # Función para editar una peña
    def on_update_collaborator_clicked(self, widget):
        self.presenter.on_update_collaborator_clicked()


    def build_collaborator_form(self, collaborator=None):
        self.collaborator_dialog = Gtk.Dialog(
            title=f"{"Añadir" if collaborator is None else "Editar"} colaborador",
            transient_for=self.main_window,
            modal=True
        )
        self.collaborator_dialog.set_default_size(450, 300)

        content_area = self.collaborator_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)


        # Caja principal con expansión vertical
        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        main_box.set_vexpand(True)

        buttons_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        buttons_box.set_halign(Gtk.Align.CENTER)

        cancel_button = Gtk.Button(label="Cancelar")
        cancel_button.connect("clicked", lambda btn: self.collaborator_dialog.destroy())

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.connect("clicked", lambda btn: self.on_complex_form_dialog_response(self.collaborator_dialog))

        # Mensaje de error arriba
        self.collaborator_form_error_label = Gtk.Label()
        self.collaborator_form_error_label.set_halign(Gtk.Align.START)
        self.collaborator_form_error_label.get_style_context().add_class("error-label")
        self.collaborator_form_error_label.set_markup('<span foreground="red"></span>')
        main_box.append(self.collaborator_form_error_label)


        # INSTAGRAM
        name_label = Gtk.Label()
        name_label.set_markup('<span size="small">Nombre <span foreground="red">*</span></span>')
        name_label.set_halign(Gtk.Align.START)
        main_box.append(name_label)

        self.name_entry = Gtk.Entry()
        self.name_entry.set_placeholder_text("Nombre")
        self.name_entry.connect("changed", self.on_string_changed, 60, "Nombre", self.collaborator_form_error_label, accept_button)
        if collaborator is not None:
            self.name_entry.set_text(collaborator.name)

        main_box.append(self.name_entry)


        # PRECIO DE COMPRA
        percentage_label = Gtk.Label()
        percentage_label.set_markup('<span size="small">Porcentaje de beneficio <span foreground="red">*</span></span>')
        percentage_label.set_halign(Gtk.Align.START)
        main_box.append(percentage_label)

        self.percentage_entry = Gtk.Entry()
        self.percentage_entry.set_placeholder_text("Porcentaje")
        if collaborator is not None:
            self.percentage_entry.set_text(str(collaborator.profit_percentage))

        self.percentage_entry.connect("changed", self.on_big_decimal_changed, "Porcentaje", self.collaborator_form_error_label, accept_button)
        main_box.append(self.percentage_entry)

        # --- Separador ---
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        content_area.append(separator)

        # --- Caja de botones pegados abajo ---
        content_area.append(main_box)
        buttons_box.append(cancel_button)
        buttons_box.append(accept_button)

        content_area.append(buttons_box)

        self.collaborator_dialog.present()

    def on_big_decimal_changed(self, widget, attribute, error_label, okey_button_label):
        self.presenter.on_big_decimal_changed(widget.get_text(), attribute, error_label, okey_button_label)

    def on_string_changed(self, widget, attribute, max_chars, error_label, okey_button_label):
        self.presenter.on_string_changed(widget, attribute, max_chars, error_label, okey_button_label)


    def on_validate_collaborator_form(self):
        """
        Tu validación personalizada:
        Si algo no está bien, muestra el error.
        """
        error_message = ""

        if self.name_entry.get_text().strip() == "":
            error_message = "El nombre del inversor es obligatorio"

        # Si todo está bien, oculta el mensaje
        if error_message:
            self.collaborator_form_error_label.set_markup(f'<span foreground="red">{error_message}</span>')
            self.collaborator_form_error_label.set_visible(True)
            return False
        else:
            self.collaborator_form_error_label.set_visible(False)
            return True

    def on_complex_form_dialog_response(self, dialog):
        self.presenter.check_collaborator_percentage(self.percentage_entry.get_text().strip())

    def on_collaborator_form_validated(self, option):

        if self.on_validate_collaborator_form():
            name = self.name_entry.get_text()
            percentage = float(self.percentage_entry.get_text().strip())

            if option == "add":
                self.presenter.add_collaborator(name, percentage)
            elif option == "update":
                self.presenter.update_collaborator(name, percentage)

            self.collaborator_dialog.destroy()

    # Función para borrar una peña
    def on_delete_collaborator_clicked(self, widget):
        delete_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas borrar a '{self.collaborator.name}'?"
        )

        # Añadir botones de Cancelar y Borrar
        delete_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Borrar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        delete_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        delete_dialog.connect("response", self.on_delete_collaborator_response)
        delete_dialog.present()

    # Función que maneja la lógica de borrar una peña
    def on_delete_collaborator_response(self, delete_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.presenter.on_delete_collaborator(self.collaborator)

        delete_dialog.close()


    # Función para indicar un fallo de servicio
    def on_service_failure(self, message):
        """Muestra un fallo de servicio"""

        self.service_failure_dialog = Gtk.MessageDialog(title="Fallo de servicio",
                                                        message_type=Gtk.MessageType.ERROR,
                                                        transient_for=self.main_window,
                                                        modal=True)

        self.service_failure_dialog.set_default_size(400, 500)
        content_area = self.service_failure_dialog.get_content_area()

        service_failure_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        service_failure_box.set_margin_top(20)
        service_failure_box.set_margin_bottom(20)
        service_failure_box.set_margin_start(20)
        service_failure_box.set_margin_end(20)
        service_failure_box.set_halign(Gtk.Align.CENTER)
        service_failure_box.set_valign(Gtk.Align.CENTER)

        icon_path = "icons/service_failure.png"
        icon_image = Gtk.Image.new_from_file(icon_path)
        icon_image.set_pixel_size(150)
        content_area.append(icon_image)

        try_again_label = Gtk.Label(halign=Gtk.Align.CENTER)
        try_again_label.set_markup(f"<span size='large'>{message}</span>")
        service_failure_box.append(try_again_label)

        content_area.append(service_failure_box)

        retry_button = Gtk.Button(label="Reintentar")
        retry_button.set_halign(Gtk.Align.CENTER)
        retry_button.set_margin_top(20)
        retry_button.set_size_request(160, 60)
        retry_button.connect("clicked", lambda _: self.presenter.retry_last_operation())
        content_area.append(retry_button)

        self.service_failure_dialog.present()

    # Función para mostrar la pantalla de carga
    def on_loading(self):
        """Muestra la pantalla de carga"""
        self.load_dialog = Gtk.MessageDialog(title="Cargando",
                                             message_type=Gtk.MessageType.ERROR,
                                             buttons=Gtk.ButtonsType.NONE,
                                             transient_for=self.main_window,
                                             modal=True)

        self.load_dialog.set_default_size(400, 500)
        content_area = self.load_dialog.get_content_area()
        self.load_dialog.set_sensitive(False)

        loading_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        loading_box.set_margin_top(20)
        loading_box.set_margin_bottom(20)
        loading_box.set_margin_start(20)
        loading_box.set_margin_end(20)
        loading_box.set_halign(Gtk.Align.CENTER)
        loading_box.set_valign(Gtk.Align.CENTER)

        icon_path = "icons/loading_icon.png"
        icon_image = Gtk.Image.new_from_file(icon_path)
        icon_image.set_pixel_size(150)
        content_area.append(icon_image)

        loading_label = Gtk.Label(halign=Gtk.Align.CENTER)
        loading_label.set_markup("<span size='large'>Cargando...</span>")
        loading_box.append(loading_label)

        content_area.append(loading_box)

        self.load_spinner = Gtk.Spinner()
        self.load_spinner.set_halign(Gtk.Align.CENTER)
        self.load_spinner.set_margin_top(20)
        self.load_spinner.set_size_request(80, 80)
        self.load_spinner.start()  # Iniciar el spinner
        content_area.append(self.load_spinner)

        self.load_dialog.present()

    # Función para parar el spinner de carga
    def stop_loading(self):
        """Detiene el spinner y cierra el diálogo de carga."""
        if hasattr(self, 'load_spinner') and self.load_spinner:
            self.load_spinner.stop()
        if hasattr(self, 'load_dialog') and self.load_dialog:
            self.load_dialog.destroy()
        self.main_window.set_sensitive(True)

    # Función para aplicar el css
    def apply_css(self):
        css = b"""

        .custom-button {
            background-color: white;
            border-radius: 15px;
            border: 1px solid #ccc;
        }

        .confirm-button {
            background: #28a745;
            color: white; 
            border-radius: 15px;
            border: 1px solid #ccc;
        }

        .confirm-button:hover {
            background-color: #218838; 
        }

        .confirm-button:active {
            background-color: #1e7e34; 
            border-color: #999;        
        }

        .cancel-button {
            background: red;
            color: white;
            border-radius: 15px;
            border: 1px solid #ccc;
        }

        .cancel-button:hover {
            background-color: #cc0000; 
        }

        .cancel-button:active {
            background-color: #990000; 
            border-color: #999;       
        }

        .custom-button:hover {
            background-color: #f0f0f0;  
        }

        .custom-button:active {
            background-color: #d0d0d0; 
            border-color: #999;  
        }     

        .custom-search-button {
            background-color: #007BFF;  
            color: white;              
            border-radius: 8px;      
            border: none;               
            padding: 6px 12px;           
            font-weight: bold;           
        }

        .custom-search-button:hover {
            background-color: #0056b3;   
        }
        .custom-search-button:active {
            background-color: #004494;  
        }

        .collaborators-list-row {
            margin-bottom: 4px;
        }

        .collaborator-item {
            border-radius: 10px;
            border: 1px solid #ddd;
            background: #fff;
            padding: 10px;
        }

        .collaborator-item:hover {
            background: #f5f5f5;
        }

        .collaborator-item Gtk.Label {
            font-size: 20px;
        }

        .completed-row {
            background-color: #eeeeee;
            color: #888888;
        }

        .shirt-type-link {
            font-size: 15px;   
        }

        .link {
            /* color: #007BFF; */
            text-decoration: underline;
        }
        
        .total-profit-box {
            background-color: #f5f5f5;
            border: 1px solid #ccc;
            border-radius: 8px;
            padding: 6px 12px;
        }

        """
        style_provider = Gtk.CssProvider()
        style_provider.load_from_data(css)
        Gtk.StyleContext.add_provider_for_display(
            Gdk.Display.get_default(),
            style_provider,
            Gtk.STYLE_PROVIDER_PRIORITY_APPLICATION
        )