import gi
from gi.overrides.Gdk import Gdk

gi.require_version('Gtk', '4.0')  # establecer versión
from gi.repository import Gtk, Gio, GObject

WINDOW_PADDING = 12

class ShirtTypesView:

    def _camel_to_snake(self, name):
        import re
        s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
        return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

    def __init__(self, main_window: Gtk.Window):

        self.main_window = main_window

        self.main_window.set_default_size(1000, 600)

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

        self.shirt_type = None
        self.responsible = None
        self.customers_number = None
        self.shirt_type_revenue = None
        self.raffle_button = None
        self.exists_raffle = None
        self.raffle = None
        self.level = None
        self.see_raffle_dialog = None
        self.level_raised = None
        self.level_participants = None
        self.num_level = None

        # Aplica el CSS
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

        # Botón de recargar lista de camisetas
        reload_button = Gtk.Button()
        reload_button.set_margin_start(0)
        reload_button.set_icon_name("view-refresh-symbolic")
        reload_button.set_tooltip_text("Recargar peás")
        reload_button.connect("clicked", self.on_reload_clicked)
        searchbar_box.append(reload_button)

        # Separador vertical
        separator = Gtk.Separator(orientation=Gtk.Orientation.VERTICAL)
        searchbar_box.append(separator)

        # Campo de búsqueda
        self.search_entry = Gtk.SearchEntry()
        self.search_entry.set_placeholder_text("Buscar peñas")
        searchbar_box.append(self.search_entry)

        self.search_button = Gtk.Button(label="Buscar")
        self.search_button.set_tooltip_text("Buscar peñas")
        self.search_button.get_style_context().add_class("custom-search-button")
        self.search_button.connect("clicked", self.on_search_button_clicked)
        searchbar_box.append(self.search_button)

        # Añadir peña
        self.add_button = Gtk.Button()
        self.add_button.set_icon_name("list-add-symbolic")
        self.add_button.set_tooltip_text("Añadir peña")
        self.add_button.get_style_context().add_class("custom-button")
        self.add_button.connect("clicked", self.on_add_shirt_type_clicked)
        searchbar_box.append(self.add_button)

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
        """Recarga la lista de camisetas"""
        self.search_entry.set_text('')
        self.presenter.on_shirt_type_search_activate()

    # Función por si cambia la búsqueda
    def on_search_button_clicked(self, widget):
        self.presenter.on_shirt_type_search_activate()  # Llama al presenter para que procese la búsqueda

    # Función para inicializar el panel izquierdo
    def _initialize_left_panel(self):
        """Inicializa el panel izquierdo con la lista de pacientes y los controles de paginación"""

        left_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        left_box.set_margin_top(10)
        left_box.set_margin_bottom(10)
        left_box.set_margin_start(10)
        left_box.set_margin_end(10)

        self.shirt_types_flowbox = Gtk.FlowBox()
        self.shirt_types_flowbox.set_max_children_per_line(2)  # 2 columnas
        self.shirt_types_flowbox.set_selection_mode(Gtk.SelectionMode.SINGLE)
        self.shirt_types_flowbox.connect("selected-children-changed", self.on_shirt_type_selected)

        scrolled_window = Gtk.ScrolledWindow()
        scrolled_window.set_policy(Gtk.PolicyType.NEVER, Gtk.PolicyType.AUTOMATIC)
        scrolled_window.set_child(self.shirt_types_flowbox)
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
        self.presenter.on_shirt_type_previous_page()

    # Función para cambiar a la página siguiente
    def on_next_page(self, widget):
        self.presenter.on_shirt_type_next_page()

    # Función para mostrar la lista de peñas en la interfaz
    def show_shirt_types(self, shirt_types):
        """Mostrar la lista de pacientes en la interfaz"""

        child = self.shirt_types_flowbox.get_first_child()
        while child is not None:
            next_child = child.get_next_sibling()
            self.shirt_types_flowbox.remove(child)
            child = next_child

        # Construir cada celda
        for shirt_type in shirt_types:
            # Contenedor vertical para cada celda
            shirt_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=6)
            shirt_box.set_halign(Gtk.Align.START)
            shirt_box.set_valign(Gtk.Align.START)
            shirt_box.set_size_request(200, 250)
            shirt_box.add_css_class("shirt-type-item")

            # Parte superior: icono + nombre en horizontal
            header_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=6)
            header_box.set_halign(Gtk.Align.CENTER)


            name_label = Gtk.Label(label=shirt_type.name)
            name_label.set_use_markup(True)
            name_label.set_markup(f'<span size="large"><b>{shirt_type.name}</b></span>')
            name_label.set_halign(Gtk.Align.START)
            header_box.append(name_label)

            shirt_box.append(header_box)

            # Imagen grande de la prenda debajo
            shirt_image = Gtk.Image.new_from_file(f"images/{shirt_type.image}.png")
            shirt_image.set_margin_top(10)
            shirt_image.set_pixel_size(150)  # Más grande
            shirt_image.set_halign(Gtk.Align.CENTER)
            shirt_box.append(shirt_image)

            # Guardar referencia
            shirt_box.shirt_type = shirt_type

            # Añadir la celda al FlowBox
            self.shirt_types_flowbox.append(shirt_box)

    # Función por si se pulsa en una peña
    def on_shirt_type_selected(self, flowbox):
        selected_children = flowbox.get_selected_children()
        selected_child = selected_children[0]
        shirt_box = selected_child.get_child()
        shirt_type = shirt_box.shirt_type
        self.presenter.on_shirt_type_selected(shirt_type) # Todo: cambiar el self.shirt_type


    # Función para inicializar el panel derecho
    def _initialize_right_panel(self):
        """Inicializa el panel derecho"""

        right_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        right_box.set_margin_top(10)
        right_box.set_margin_bottom(10)
        right_box.set_margin_start(10)
        right_box.set_margin_end(10)

        # Detalles de la camiseta
        self.shirt_type_details_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.shirt_type_details_box.set_vexpand(True)
        right_box.append(self.shirt_type_details_box)

        return right_box

    # Función para reiniciar la pantalla de detalles de la peña
    def clear_shirt_type_details_box(self):
        while True:
            child = self.shirt_type_details_box.get_first_child()
            if child is None:
                break
            self.shirt_type_details_box.remove(child)

        self.shirt_type = None
        self.responsible = None

    # Función para mostrar los detalles de una peña
    def show_shirt_type_details(self, shirt_type, responsible):
        self.clear_shirt_type_details_box()

        self.shirt_type = shirt_type
        self.responsible = responsible

        header_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        header_separator.set_margin_top(10)
        header_separator.set_margin_bottom(10)

        self.shirt_type_details_box.append(header_separator)

        self.content_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.content_box.set_vexpand(True)

        header_box = Gtk.CenterBox()
        header_box.set_hexpand(True)
        header_box.set_halign(Gtk.Align.FILL)

        # INSTAGRAM Y ACCIONES
        shirt_type_name_label = Gtk.Label()
        shirt_type_name_label.set_markup(f'<span size="20000"><b>{self.shirt_type.name}</b></span>')
        shirt_type_name_label.set_halign(Gtk.Align.CENTER)

        actions_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

        self.edit_button = Gtk.Button()
        self.edit_button.set_icon_name("document-edit-symbolic")
        self.edit_button.set_tooltip_text("Editar camiseta")
        self.edit_button.connect("clicked", self.on_update_shirt_type_clicked)
        actions_box.append(self.edit_button)

        self.delete_button = Gtk.Button()
        self.delete_button.set_icon_name("edit-delete-symbolic")
        self.delete_button.set_tooltip_text("Eliminar camiseta")
        self.delete_button.connect("clicked", self.on_delete_shirt_clicked)

        actions_box.append(self.delete_button)

        header_box.set_center_widget(shirt_type_name_label)
        header_box.set_end_widget(actions_box)

        self.content_box.append(header_box)

        # Imagen de la peña
        image_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        image_box.set_halign(Gtk.Align.CENTER)
        shirt_image = Gtk.Image.new_from_file(f"images/{shirt_type.image}.png")
        shirt_image.set_margin_top(10)
        shirt_image.set_pixel_size(150)  # Más grande
        shirt_image.set_halign(Gtk.Align.CENTER)

        image_box.append(shirt_image)

        self.content_box.append(image_box)

        body_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        body_separator.set_margin_top(10)
        body_separator.set_margin_bottom(10)
        self.content_box.append(body_separator)

        # DESCRIPCIÓN
        if shirt_type.description:
            description_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
            description_box.set_halign(Gtk.Align.CENTER)
            description_box.set_margin_top(20)
            description_label = Gtk.Label()
            description_label.set_markup(f'<span size="12500">{self.shirt_type.description}</span>')
            description_label.set_halign(Gtk.Align.CENTER)
            description_box.append(description_label)
            self.content_box.append(description_box)


        # PRECIO BASE
        base_sales_price_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        base_sales_price_box.set_halign(Gtk.Align.CENTER)
        base_sales_price_box.set_margin_top(10 if shirt_type.description is None else 20)
        base_sales_price_label = Gtk.Label()
        base_sales_price_label.set_markup(f'<span size="12500">Precio base: {self.shirt_type.base_sales_price} €</span>')
        base_sales_price_label.set_halign(Gtk.Align.CENTER)
        base_sales_price_box.append(base_sales_price_label)
        self.content_box.append(base_sales_price_box)


        # RESPONSABLE
        if self.shirt_type.responsible_id is not None:

            responsible_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
            responsible_content.set_halign(Gtk.Align.CENTER)
            responsible_content.set_margin_top(10)

            responsible_label = Gtk.Label()
            responsible_label.set_markup(f'<span size="12500">{"Responsable: "}</span>')
            responsible_label.set_halign(Gtk.Align.CENTER)
            responsible_content.append(responsible_label)

            responsible_link = Gtk.LinkButton.new_with_label(f"{self.responsible.name}", f"{self.responsible.name}")
            responsible_link.set_halign(Gtk.Align.CENTER)
            responsible_link.connect("activate-link", self.on_responsible_clicked)
            responsible_link.get_style_context().add_class("shirt-type-link")
            responsible_link.get_style_context().add_class("link")
            responsible_content.append(responsible_link)

            self.content_box.append(responsible_content)

        # CLIENTES
        customers_number_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
        customers_number_content.set_halign(Gtk.Align.CENTER)
        customers_number_content.set_margin_top(10)

        customers_number_label = Gtk.Label()
        customers_number_label.set_markup(f'<span size="12500">{"Clientes:"}</span>')
        customers_number_label.set_halign(Gtk.Align.CENTER)
        customers_number_content.append(customers_number_label)

        customers_number = Gtk.LinkButton.new_with_label(f"{self.customers_number}", f"{self.customers_number}")
        customers_number.set_halign(Gtk.Align.CENTER)
        customers_number.connect("activate-link", self.on_customers_number_clicked)
        customers_number.get_style_context().add_class("shirt-type-link")
        customers_number.get_style_context().add_class("link")
        customers_number_content.append(customers_number)

        self.content_box.append(customers_number_content)

        # BENEFICIO TOTAL
        revenue_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        revenue_box.set_halign(Gtk.Align.CENTER)
        revenue_box.set_margin_top(10)

        revenue_label = Gtk.Label()
        revenue_label.set_markup(f'<span size="12500">Beneficio total: {self.shirt_type_revenue} €</span>')
        revenue_label.set_halign(Gtk.Align.CENTER)
        revenue_box.append(revenue_label)
        self.content_box.append(revenue_box)

        self.shirt_type_details_box.append(self.content_box)

        # DATOS DEL SORTEO
        footer_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=20)
        footer_box.set_halign(Gtk.Align.CENTER)
        footer_box.set_valign(Gtk.Align.END)  # Anclado abajo del todo
        footer_box.set_margin_top(20)

        self.raffle_button = Gtk.Button(label=f"{"Ver sorteo" if self.exists_raffle else "Añadir Sorteo"}")
        self.raffle_button.set_tooltip_text(f"{"Ver sorteo" if self.exists_raffle else "Añadir Sorteo"}")

        self.raffle_button.connect("clicked", self.on_raffle_button_clicked)

        footer_box.append(self.raffle_button)

        self.shirt_type_details_box.append(footer_box)

    def on_add_shirt_type_clicked(self, widget):
        self.presenter.on_add_shirt_type_clicked()

    def on_update_shirt_type_clicked(self, widget):
        self.presenter.on_update_shirt_type_clicked()

    def build_shirt_type_form(self, shirt_type=None):
        self.shirt_type_dialog = Gtk.Dialog(
            title=f"{'Añadir' if shirt_type is None else 'Editar'} peña",
            transient_for=self.main_window,
            modal=True
        )
        self.shirt_type_dialog.set_default_size(500, 600)

        self.shirt_type_dialog.set_title(f"{'Añadir' if shirt_type is None else 'Editar'} peña")

        cancel_button = Gtk.Button(label="Cancelar")
        cancel_button.connect("clicked", lambda btn: self.shirt_type_dialog.destroy())

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.connect("clicked", lambda btn: self.on_complex_form_dialog_response(self.shirt_type_dialog))

        content_area = self.shirt_type_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        main_box.set_vexpand(True)

        # --- Etiqueta de error ---
        self.shirt_type_form_error_label = Gtk.Label()
        self.shirt_type_form_error_label.set_halign(Gtk.Align.START)
        self.shirt_type_form_error_label.get_style_context().add_class("error-label")
        self.shirt_type_form_error_label.set_markup('<span foreground="red"></span>')
        main_box.append(self.shirt_type_form_error_label)

        # --- Nombre ---
        name_label = Gtk.Label()
        name_label.set_markup('<span size="small">Nombre <span foreground="red">*</span></span>')
        name_label.set_halign(Gtk.Align.START)
        main_box.append(name_label)

        self.name_entry = Gtk.Entry()
        self.name_entry.set_placeholder_text("Nombre")
        if shirt_type:
            self.name_entry.set_text(shirt_type.name)

        self.name_entry.connect("changed", self.on_string_changed, 60, "Nombre", self.shirt_type_form_error_label, accept_button)
        main_box.append(self.name_entry)

        # --- Descripción ---
        description_label = Gtk.Label()
        description_label.set_markup('<span size="small">Descripción</span>')
        description_label.set_halign(Gtk.Align.START)
        main_box.append(description_label)

        self.description_entry = Gtk.Entry()
        self.description_entry.set_placeholder_text("Descripción")
        if shirt_type and shirt_type.description:
            self.description_entry.set_text(shirt_type.description)

        self.description_entry.connect("changed", self.on_string_changed, 2000, "Descripción", self.shirt_type_form_error_label, accept_button)
        main_box.append(self.description_entry)

        # --- Precio base ---
        base_price_label = Gtk.Label()
        base_price_label.set_markup('<span size="small">Precio base <span foreground="red">*</span></span>')
        base_price_label.set_halign(Gtk.Align.START)
        main_box.append(base_price_label)

        self.base_price_entry = Gtk.Entry()
        self.base_price_entry.set_placeholder_text("Precio base")
        if shirt_type:
            self.base_price_entry.set_text(str(shirt_type.base_sales_price))

        self.base_price_entry.connect("changed", self.on_big_decimal_changed, "Precio base", self.shirt_type_form_error_label, accept_button)
        main_box.append(self.base_price_entry)

        # --- Número necesario ---
        free_label = Gtk.Label()
        free_label.set_markup('<span size="small">Número necesario para que sea gratis <span foreground="red">*</span></span>')
        free_label.set_halign(Gtk.Align.START)
        main_box.append(free_label)

        self.free_shirt_people_entry = Gtk.Entry()
        self.free_shirt_people_entry.set_placeholder_text("Número necesario")
        if shirt_type:
            self.free_shirt_people_entry.set_text(str(shirt_type.free_shirt_people))

        self.free_shirt_people_entry.connect("changed", self.on_int_changed, "Número de gente", self.shirt_type_form_error_label, accept_button)
        main_box.append(self.free_shirt_people_entry)

        # --- Separador y botones ---
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        content_area.append(separator)
        content_area.append(main_box)

        buttons_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        buttons_box.set_halign(Gtk.Align.CENTER)


        buttons_box.append(cancel_button)
        buttons_box.append(accept_button)
        content_area.append(buttons_box)

        self.shirt_type_dialog.present()

    def on_big_decimal_changed(self, widget, attribute, error_label, okey_button_label):
        self.presenter.on_big_decimal_changed(widget.get_text(), attribute, error_label, okey_button_label)

    def on_int_changed(self, widget, attribute, error_label, okey_button_label):
        self.presenter.on_int_changed(widget.get_text(), attribute, error_label, okey_button_label)

    def on_string_changed(self, widget, attribute, max_chars, error_label, okey_button_label):
        self.presenter.on_string_changed(widget, attribute, max_chars, error_label, okey_button_label)

    def on_validate_shirt_type_form(self):
        """
        Tu validación personalizada:
        Si algo no está bien, muestra el error.
        """
        error_message = ""

        if self.name_entry.get_text().strip() == "":
            error_message = "El nombre de la peña es obligatorio"

        elif self.base_price_entry.get_text().strip() == "":
            error_message = "El precio base de la peña es obligatorio"

        elif self.free_shirt_people_entry.get_text().strip() == "":
            error_message = "El número de personas traídas para llevarse gratis la camiseta es obligatorio"

        # Si todo está bien, oculta el mensaje
        if error_message:
            self.shirt_type_form_error_label.set_markup(f'<span foreground="red">{error_message}</span>')
            self.shirt_type_form_error_label.set_visible(True)
            return False
        else:
            self.shirt_type_form_error_label.set_visible(False)
            return True

    def on_complex_form_dialog_response(self, dialog):
        self.presenter.on_shirt_type_form_completed()

    def on_shirt_type_form_validated(self, option):

        if self.on_validate_shirt_type_form():
            name = self.name_entry.get_text()
            description = self.description_entry.get_text() if self.description_entry.get_text().strip() != "" else None
            base_sales_price = float(self.base_price_entry.get_text().strip())
            image = self._camel_to_snake(name)
            free_shirt_people = int(self.free_shirt_people_entry.get_text())

            if option == "add":
                self.presenter.add_shirt_type(name, image, base_sales_price, description, free_shirt_people)
            elif option == "update":
                self.presenter.update_shirt_type(name, image, base_sales_price, description, free_shirt_people)

            self.shirt_type_dialog.destroy()

    # Función para borrar una peña
    def on_delete_shirt_clicked(self, widget):
        delete_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas borrar la peña '{self.shirt_type.name}'?"
        )

        # Añadir botones de Cancelar y Borrar
        delete_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Borrar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        delete_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        delete_dialog.connect("response", self.on_delete_shirt_response)
        delete_dialog.present()

    # Función que maneja la lógica de borrar una peña
    def on_delete_shirt_response(self, delete_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.presenter.on_delete_shirt_type(self.shirt_type)

        delete_dialog.close()

    # Función por si se pulsa en el link del responsable
    def on_responsible_clicked(self, widget):
        self.presenter.on_shirt_type_responsible_clicked(self.responsible)

    def on_customers_number_clicked(self, widget):
        self.presenter.on_customers_number_clicked(self.shirt_type)

    # Función por si se pulsa en el botón de ver/añadir sorteo
    def on_raffle_button_clicked(self, widget):
        self.presenter.on_raffle_clicked(self.exists_raffle)

    def build_raffle_view(self):

        self.see_raffle_dialog = Gtk.Dialog(
            title=f"Sorteo de {self.shirt_type.name}",
            transient_for=self.main_window,
            modal=True
        )
        self.see_raffle_dialog.set_default_size(600, 600)

        content_area = self.see_raffle_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        content_area.append(main_box)

        header_box = Gtk.CenterBox()
        header_box.set_hexpand(True)
        header_box.set_halign(Gtk.Align.FILL)

        if self.raffle.description:
            description_label = Gtk.Label()
            description_label.set_markup(
                f'<span size="12500">{self.raffle.description}</span>')
            header_box.set_center_widget(description_label)

        actions_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

        self.edit_raffle_button = Gtk.Button()
        self.edit_raffle_button.set_icon_name("document-edit-symbolic")
        self.edit_raffle_button.set_tooltip_text("Editar sorteo")
        self.edit_raffle_button.connect("clicked", self.on_update_raffle_clicked)
        actions_box.append(self.edit_raffle_button)

        self.delete_raffle_button = Gtk.Button()
        self.delete_raffle_button.set_icon_name("edit-delete-symbolic")
        self.delete_raffle_button.set_tooltip_text("Eliminar sorteo")
        self.delete_raffle_button.connect("clicked", self.on_delete_raffle_clicked)

        actions_box.append(self.delete_raffle_button)

        header_box.set_end_widget(actions_box)

        price_label = Gtk.Label()
        price_label.set_markup(f'<span size="12500">Precio de participación: {self.raffle.participation_price} €</span>')

        if self.raffle.description:
            main_box.append(header_box)
            main_box.append(price_label)
        else:
            header_box.set_center_widget(price_label)
            main_box.append(header_box)


        separator1 = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        main_box.append(separator1)

        level_box = Gtk.CenterBox()
        level_box.set_hexpand(True)
        level_box.set_halign(Gtk.Align.FILL)
        level_box.set_margin_bottom(20)


        levels_label = Gtk.Label()
        levels_label.set_markup(f'<span size="20000"><b>NIVELES</b></span>')

        self.add_level_button = Gtk.Button()
        self.add_level_button.set_icon_name("list-add-symbolic")
        self.add_level_button.set_tooltip_text("Añadir nivel")
        self.add_level_button.get_style_context().add_class("custom-button")
        self.add_level_button.connect("clicked", self.on_add_level_clicked)

        level_box.set_center_widget(levels_label)
        level_box.set_end_widget(self.add_level_button)
        main_box.append(level_box)

        # Contenedor para la lista
        list_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        list_box.set_halign(Gtk.Align.CENTER)
        list_box.set_vexpand(True)
        main_box.append(list_box)

        num_level = 1

        for level in self.raffle.battle_pass:
            row = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
            row.set_margin_bottom(15)

            level_button = Gtk.Button(label=f"Nivel {num_level}")
            level_button.connect("clicked", self.on_level_clicked, level, num_level)

            num_level += 1

            row.append(level_button)
            list_box.append(row)

        # Separador
        separator2 = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        main_box.append(separator2)

        # Botón Aceptar

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.set_halign(Gtk.Align.CENTER)
        accept_button.connect("clicked", lambda btn: self.see_raffle_dialog.destroy())
        main_box.append(accept_button)

        self.see_raffle_dialog.present()

    def on_update_raffle_clicked(self, widget):
        self.presenter.on_edit_raffle_clicked()

    def on_delete_raffle_clicked(self, widget):
        # Función para borrar un sorteo
        delete_dialog = Gtk.MessageDialog(
                transient_for=self.main_window,
                modal=True,
                message_type=Gtk.MessageType.QUESTION,
                buttons=Gtk.ButtonsType.NONE,
                text=f"¿Estás seguro de que deseas el sorteo de '{self.shirt_type.name}'?"
            )

        # Añadir botones de Cancelar y Borrar
        delete_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Borrar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        delete_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        delete_dialog.connect("response", self.on_delete_raffle_response)
        delete_dialog.present()

    # Función que maneja la lógica de borrar una peña
    def on_delete_raffle_response(self, delete_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.presenter.delete_raffle(self.raffle)

        delete_dialog.close()


    def build_raffle_form(self, raffle=None):
        self.raffle_dialog = Gtk.Dialog(
            title=f"{'Añadir' if raffle is None else 'Editar'} sorteo",
            transient_for=self.main_window,
            modal=True
        )
        self.raffle_dialog.set_default_size(500, 600)

        self.raffle_dialog.set_title(f"{'Añadir' if raffle is None else 'Editar'} sorteo")

        cancel_button = Gtk.Button(label="Cancelar")
        cancel_button.connect("clicked", lambda btn: self.raffle_dialog.destroy())

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.connect("clicked", lambda btn: self.on_raffle_form_dialog_response(self.raffle_dialog))

        content_area = self.raffle_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        main_box.set_vexpand(True)

        # --- Etiqueta de error ---
        self.raffle_form_error_label = Gtk.Label()
        self.raffle_form_error_label.set_halign(Gtk.Align.START)
        self.raffle_form_error_label.get_style_context().add_class("error-label")
        self.raffle_form_error_label.set_markup('<span foreground="red"></span>')
        main_box.append(self.raffle_form_error_label)

        # --- Nombre ---
        participation_price_label = Gtk.Label()
        participation_price_label.set_markup('<span size="small">Precio de la participación <span foreground="red">*</span></span>')
        participation_price_label.set_halign(Gtk.Align.START)
        main_box.append(participation_price_label)

        self.participation_price = Gtk.Entry()
        self.participation_price.set_placeholder_text("Precio")
        if raffle:
            self.participation_price.set_text(str(raffle.participation_price))

        self.participation_price.connect("changed", self.on_big_decimal_changed, "Precio", self.raffle_form_error_label, accept_button)
        main_box.append(self.participation_price)

        # --- Descripción ---
        description_label = Gtk.Label()
        description_label.set_markup('<span size="small">Descripción</span>')
        description_label.set_halign(Gtk.Align.START)
        main_box.append(description_label)

        self.raffle_description_entry = Gtk.Entry()
        self.raffle_description_entry.set_placeholder_text("Descripción")
        if raffle and raffle.description:
            self.raffle_description_entry.set_text(raffle.description)

        self.raffle_description_entry.connect("changed", self.on_string_changed, 2000, "Descripción", self.raffle_form_error_label, accept_button)
        main_box.append(self.raffle_description_entry)

        # --- Separador y botones ---
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        content_area.append(separator)
        content_area.append(main_box)

        buttons_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        buttons_box.set_halign(Gtk.Align.CENTER)

        buttons_box.append(cancel_button)
        buttons_box.append(accept_button)
        content_area.append(buttons_box)

        self.raffle_dialog.present()


    def on_validate_raffle_form(self):
        """
        Tu validación personalizada:
        Si algo no está bien, muestra el error.
        """
        error_message = ""

        if self.participation_price.get_text().strip() == "":
            error_message = "El precio de participación del sorteo es obligatorio"


        # Si todo está bien, oculta el mensaje
        if error_message:
            self.raffle_form_error_label.set_markup(f'<span foreground="red">{error_message}</span>')
            self.raffle_form_error_label.set_visible(True)
            return False
        else:
            self.raffle_form_error_label.set_visible(False)
            return True

    def on_raffle_form_dialog_response(self, dialog):
        self.presenter.on_raffle_form_completed()

    def on_raffle_form_validated(self, option):

        if self.on_validate_raffle_form():
            participation_price = float(self.participation_price.get_text().strip())
            description = self.raffle_description_entry.get_text() if self.raffle_description_entry.get_text().strip() != "" else None

            if option == "add":
                self.presenter.add_raffle(participation_price, description)
            elif option == "update":
                self.presenter.update_raffle(participation_price, description)

            self.raffle_dialog.destroy()


    def on_level_clicked(self, widget, level, num_level):
        self.presenter.on_level_clicked(level, num_level)

    def build_level_view(self):
        self.see_level_dialog = Gtk.Dialog(
            title="Nivel",
            transient_for=self.main_window,
            modal=True
        )
        self.see_level_dialog.set_default_size(600, 600)

        content_area = self.see_level_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        main_box.set_vexpand(True)
        content_area.append(main_box)

        # HEADER
        header_box = Gtk.CenterBox()
        header_box.set_hexpand(True)
        header_box.set_halign(Gtk.Align.FILL)

        name_label = Gtk.Label()
        name_label.set_markup(f'<span size="15000"><b>Nivel {self.num_level} del sorteo de {self.shirt_type.name}</b></span>')
        header_box.set_center_widget(name_label)

        actions_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

        self.edit_level_button = Gtk.Button()
        self.edit_level_button.set_icon_name("document-edit-symbolic")
        self.edit_level_button.set_tooltip_text("Editar nivel")
        self.edit_level_button.connect("clicked", self.on_update_level_clicked)
        actions_box.append(self.edit_level_button)

        self.delete_level_button = Gtk.Button()
        self.delete_level_button.set_icon_name("edit-delete-symbolic")
        self.delete_level_button.set_tooltip_text("Eliminar nivel")
        self.delete_level_button.connect("clicked", self.on_delete_level_clicked)
        actions_box.append(self.delete_level_button)

        header_box.set_end_widget(actions_box)
        main_box.append(header_box)

        if self.level.level_description:
            description_label = Gtk.Label()
            description_label.set_markup(
                f'<span size="12500">{self.level.level_description}</span>')
            main_box.append(description_label)

        price_label = Gtk.Label()
        price_label.set_markup(f'<span size="12500">Coste del premio: {self.level.price} €</span>')
        main_box.append(price_label)

        necessary_participants_label = Gtk.Label()
        necessary_participants_label.set_markup(
            f'<span size="12500">Mínimo de participantes necesario: {self.level_participants}/{self.level.necessary_participants}</span>')
        main_box.append(necessary_participants_label)


        # ESPACIADOR ELÁSTICO
        spacer = Gtk.Box()
        spacer.set_vexpand(True)
        main_box.append(spacer)

        # FOOTER
        footer_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        footer_box.set_hexpand(True)
        footer_box.set_valign(Gtk.Align.END)

        separator2 = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        footer_box.append(separator2)

        # GANADOR
        if self.level.winner_id is not None:
            self.winner_box = Gtk.CenterBox()
            self.winner_box.set_margin_top(10)
            self.winner_box.set_hexpand(True)
            self.winner_box.set_halign(Gtk.Align.FILL)

            winner_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
            winner_content.set_halign(Gtk.Align.CENTER)

            winner_label = Gtk.Label()
            winner_label.set_markup('<span size="12500">Ganador: </span>')
            winner_label.set_halign(Gtk.Align.CENTER)

            winner_link = Gtk.LinkButton.new_with_label(
                f"{self.level.winner_instagram}", f"{self.level.winner_instagram}"
            )
            winner_link.set_halign(Gtk.Align.CENTER)
            winner_link.connect("activate-link", self.on_winner_link_clicked)
            winner_link.get_style_context().add_class("convincing-friend-link")
            winner_link.get_style_context().add_class("link")


            winner_content.append(winner_label)
            winner_content.append(winner_link)
            self.winner_box.set_center_widget(winner_content)

            footer_box.append(self.winner_box)

        play_button = Gtk.Button(label="Sortear")
        play_button.set_halign(Gtk.Align.CENTER)
        play_button.set_sensitive(self.level_raised)
        play_button.connect("clicked", self.on_play_button_clicked)
        footer_box.append(play_button)

        main_box.append(footer_box)

        self.see_level_dialog.present()

    def on_update_level_clicked(self, widget):
        self.presenter.on_update_level_clicked()

    def on_winner_link_clicked(self, widget):
        self.presenter.on_winner_link_clicked(self.level)

    def on_play_button_clicked(self, widget):
        # Función para sortear
        play_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas sortear el ganador?\nEl ganador anterior se borrará"
        )

        # Añadir botones de Cancelar y Borrar
        play_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Sortear", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        play_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        play_dialog.connect("response", self.on_play_level_response)
        play_dialog.present()

    # Función que maneja la lógica de borrar una peña
    def on_play_level_response(self, play_dialog, response):
        if response == Gtk.ResponseType.OK:
            self.presenter.play_level(self.level)

        play_dialog.close()

    def on_winner_response(self, instagram):
        winner_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"El ganador es {instagram}"
        )

        # Añadir botones de Cancelar y Borrar
        winner_dialog.add_buttons(
            "Aceptar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        winner_dialog.set_default_response(Gtk.ResponseType.OK)

        winner_dialog.connect("response", self.on_winner_accepted)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        winner_dialog.present()

    def on_winner_accepted(self, winner_dialog, response):
        winner_dialog.destroy()
        self.presenter.on_raffle_selected()

    def on_delete_level_clicked(self, widget):
        # Función para borrar un sorteo
        delete_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas el nivel?"
        )

        # Añadir botones de Cancelar y Borrar
        delete_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Borrar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        delete_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        delete_dialog.connect("response", self.on_delete_level_response)
        delete_dialog.present()

    # Función que maneja la lógica de borrar una peña
    def on_delete_level_response(self, delete_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.see_level_dialog.destroy()
            self.presenter.delete_level(self.level)
        delete_dialog.close()

    def on_add_level_clicked(self, widget):
        self.presenter.on_add_level_clicked()

    def build_level_form(self, level=None):
        self.level_dialog = Gtk.Dialog(
            title=f"{'Añadir' if level is None else 'Editar'} nivel",
            transient_for=self.main_window,
            modal=True
        )
        self.level_dialog.set_default_size(500, 600)

        self.level_dialog.set_title(f"{'Añadir' if level is None else 'Editar'} nivel")

        cancel_button = Gtk.Button(label="Cancelar")
        cancel_button.connect("clicked", lambda btn: self.level_dialog.destroy())

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.connect("clicked", lambda btn: self.on_level_form_dialog_response(self.level_dialog))

        content_area = self.level_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        main_box.set_vexpand(True)

        # --- Etiqueta de error ---
        self.level_form_error_label = Gtk.Label()
        self.level_form_error_label.set_halign(Gtk.Align.START)
        self.level_form_error_label.get_style_context().add_class("error-label")
        self.level_form_error_label.set_markup('<span foreground="red"></span>')
        main_box.append(self.level_form_error_label)


        # --- Descripción ---
        description_label = Gtk.Label()
        description_label.set_markup('<span size="small">Descripción</span>')
        description_label.set_halign(Gtk.Align.START)
        main_box.append(description_label)

        self.level_description_entry = Gtk.Entry()
        self.level_description_entry.set_placeholder_text("Descripción")
        if level and level.level_description:
            self.level_description_entry.set_text(level.level_description)

        self.level_description_entry.connect("changed", self.on_string_changed, 2000, "Descripción", self.level_form_error_label, accept_button)
        main_box.append(self.level_description_entry)

        self.necessary_participants = None


        # --- Precio del premio ---
        price_label = Gtk.Label()
        price_label.set_markup('<span size="small">Coste del premio <span foreground="red">*</span></span>')
        price_label.set_halign(Gtk.Align.START)
        main_box.append(price_label)

        self.price_entry = Gtk.Entry()
        self.price_entry.set_placeholder_text("Precio")
        if level:
            self.price_entry.set_text(str(level.price))

        self.price_entry.connect("changed", self.on_big_decimal_changed, "Precio", self.level_form_error_label, accept_button)
        main_box.append(self.price_entry)

        necessary_participants_label = Gtk.Label()
        necessary_participants_label.set_markup('<span size="small">Número mínimo de participantes <span foreground="red">*</span></span>')
        necessary_participants_label.set_halign(Gtk.Align.START)
        main_box.append(necessary_participants_label)

        self.necessary_participants_entry = Gtk.Entry()
        self.necessary_participants_entry.set_placeholder_text("Número mínimo")
        if level:
            self.necessary_participants_entry.set_text(str(level.necessary_participants))

        self.necessary_participants_entry.connect("changed", self.on_int_changed, "Número de participantes", self.level_form_error_label, accept_button)
        main_box.append(self.necessary_participants_entry)

        # --- Separador y botones ---
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        content_area.append(separator)
        content_area.append(main_box)

        buttons_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        buttons_box.set_halign(Gtk.Align.CENTER)

        buttons_box.append(cancel_button)
        buttons_box.append(accept_button)
        content_area.append(buttons_box)

        self.level_dialog.present()


    def on_validate_level_form(self):
        """
        Tu validación personalizada:
        Si algo no está bien, muestra el error.
        """
        error_message = ""

        if self.price_entry.get_text().strip() == "":
            error_message = "El coste del nivel del sorteo es obligatorio"

        elif self.necessary_participants_entry.get_text().strip() == "":
            error_message = "El número mínimo de participantes es obligatorio"


        # Si todo está bien, oculta el mensaje
        if error_message:
            self.level_form_error_label.set_markup(f'<span foreground="red">{error_message}</span>')
            self.level_form_error_label.set_visible(True)
            return False
        else:
            self.level_form_error_label.set_visible(False)
            return True

    def on_level_form_dialog_response(self, dialog):
        self.presenter.on_level_form_completed()

    def on_level_form_validated(self, option):

        if self.on_validate_level_form():
            description = self.level_description_entry.get_text() if self.level_description_entry.get_text().strip() != "" else None
            price = float(self.price_entry.get_text().strip())
            necessary_participants = int(self.necessary_participants_entry.get_text().strip())

            if option == "add":
                self.presenter.add_level(description, price, necessary_participants)
            elif option == "update":
                self.presenter.update_level(description, price, necessary_participants)

            self.level_dialog.destroy()


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

        .shirt-type-list-row {
            margin-bottom: 4px;
        }
        
        .shirt-type-item {
            border-radius: 12px;
            border: 1px solid #ddd;
            background: #fff;
            padding: 12px;
        }
            
        .shirt-type-item:hover {
            background: #f5f5f5;
        }
        
        .shirt-type-item Gtk.Label {
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

        """
        style_provider = Gtk.CssProvider()
        style_provider.load_from_data(css)
        Gtk.StyleContext.add_provider_for_display(
            Gdk.Display.get_default(),
            style_provider,
            Gtk.STYLE_PROVIDER_PRIORITY_APPLICATION
        )