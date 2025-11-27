from decimal import Decimal

import gi
from gi.overrides.Gdk import Gdk

gi.require_version('Gtk', '4.0')  # establecer versión
from gi.repository import Gtk, Gio, GObject

WINDOW_PADDING = 12

class ShirtsView:
    def __init__(self, main_window: Gtk.Window):

        self.main_window = main_window

        # Presentador que manejará la vista
        self.presenter = None

        self.customer_convinced_friends_number = None
        self.customer_convinced_friends_bought = None

        self.buy_button_label = None
        self.buy_tooltip_label = None
        self.current_price = None
        self.sale_price_label = None

        self.shirt = None
        self.responsible = None

        self.filter_shirt_type_id = None
        self.filter_collaborator_id = None
        self.filter_shirt_size = None
        self.filter_is_responsible = None

        self.shirt_type_combo = None
        self.collaborator_combo = None
        self.size_combo = None
        self.responsible_switch = None

        self.main_window.set_default_size(1000, 600)
        self.main_window.set_title("Camisetas")

        # Contenedor principal
        self.main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=20)
        self.main_box.set_margin_top(WINDOW_PADDING)
        self.main_box.set_margin_bottom(WINDOW_PADDING)
        self.main_box.set_margin_start(WINDOW_PADDING)
        self.main_box.set_margin_end(WINDOW_PADDING)

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

        # Aplica el CSS
        self.apply_css()

        self.widget = self.main_box

    # Función para cambiar el presentador
    def set_presenter(self, presenter):
        self.presenter = presenter

    # Función para crear la barra de búsqueda
    def create_searchbar(self):
        searchbar_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=6)
        searchbar_box.add_css_class("toolbar")

        # Botón del menú para cambiar de vista
        self.menu_button = Gtk.MenuButton()
        self.menu_button.set_icon_name("open-menu-symbolic")
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
        reload_button.set_tooltip_text("Recargar camisetas")
        reload_button.connect("clicked", self.on_reload_clicked)
        searchbar_box.append(reload_button)

        # Separador vertical
        separator = Gtk.Separator(orientation=Gtk.Orientation.VERTICAL)
        searchbar_box.append(separator)

        # Campo de búsqueda
        self.search_entry = Gtk.SearchEntry()
        self.search_entry.set_placeholder_text("Buscar camisetas")
        searchbar_box.append(self.search_entry)

        self.search_button = Gtk.Button(label="Buscar")
        self.search_button.set_tooltip_text("Buscar camisetas")
        self.search_button.get_style_context().add_class("custom-search-button")
        self.search_button.connect("clicked", self.on_search_button_clicked)
        searchbar_box.append(self.search_button)

        # Botón de filtrar
        self.filter_button = Gtk.Button(label="Filtrar")
        self.filter_button.set_tooltip_text("Filtrar camisetas")
        self.filter_button.get_style_context().add_class("custom-button")
        self.filter_button.connect("clicked", self.on_filter_clicked)
        searchbar_box.append(self.filter_button)

        # Botónd de añadir camiseta
        self.add_button = Gtk.Button()
        self.add_button.set_icon_name("list-add-symbolic")
        self.add_button.set_tooltip_text("Añadir camiseta")
        self.add_button.get_style_context().add_class("custom-button")
        self.add_button.connect("clicked", self.on_add_shirt_clicked)
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

    # Función para recargar la lista de camisetas
    def on_reload_clicked(self, widget):
        self.search_entry.set_text('')
        self.presenter.on_shirt_search_activate()

    # Función por si cambia la búsqueda
    def on_search_button_clicked(self, widget):
        self.presenter.on_shirt_search_activate()  # Llama al presenter para que procese la búsqueda

    # Función para inicializar el panel izquierdo
    def _initialize_left_panel(self):
        left_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        left_box.set_margin_top(10)
        left_box.set_margin_bottom(10)
        left_box.set_margin_start(10)
        left_box.set_margin_end(10)

        # Lista paginada de camisetas
        self.shirts_listbox = Gtk.ListBox()
        self.shirts_listbox.set_selection_mode(Gtk.SelectionMode.SINGLE)
        self.shirts_listbox.connect("row-activated", self.on_shirt_selected)  # Conectar el evento de clic

        scrolled_window = Gtk.ScrolledWindow()
        scrolled_window.set_policy(Gtk.PolicyType.NEVER, Gtk.PolicyType.AUTOMATIC)
        scrolled_window.set_child(self.shirts_listbox)
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
        self.presenter.on_shirt_previous_page()

    # Función para cambiar a la página siguiente
    def on_next_page(self, widget):
        self.presenter.on_shirt_next_page()

    # Función para mostrar la lista de camisetas en la interfaz
    def show_shirts(self, shirts):
        while True:
            row = self.shirts_listbox.get_row_at_index(0)
            if row is None:
                break
            self.shirts_listbox.remove(row)

        for shirt in shirts:
            row = Gtk.ListBoxRow()
            row.add_css_class("shirt-list-row")

            self.presenter.apply_shirts_bought_styles(shirt, row)

            shirt_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
            row.set_child(shirt_box)

            customer_avatar = Gtk.Image.new_from_icon_name("avatar-default-symbolic")
            customer_avatar.set_pixel_size(32)
            shirt_box.append(customer_avatar)

            # Caja que contiene el nombre y el código de un paciente
            customer_attributes_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL)
            shirt_box.append(customer_attributes_box)

            customer_instagram_label = Gtk.Label(label=f"<b>{shirt.customer_instagram}</b>")
            customer_instagram_label.set_use_markup(True)  # Hace que la interprete como letra negrita
            customer_instagram_label.set_halign(Gtk.Align.START)
            customer_attributes_box.append(customer_instagram_label)

            customer_label = Gtk.Label(label=f"{shirt.shirt_type_name} - Talla: {shirt.size}")
            customer_label.set_halign(Gtk.Align.START)
            customer_attributes_box.append(customer_label)

            row.shirt = shirt
            self.shirts_listbox.append(row)

    # Función por si se pulsa en una camiseta
    def on_shirt_selected(self, list_box, row):
        self.shirt = row.shirt
        self.presenter.on_shirt_selected(row.shirt)

    # Función en caso de filtrar búsqueda
    def on_filter_clicked(self, widget):
        self.presenter.on_filter_clicked()

    def build_filter_dialog(self, shirt_types, collaborators, sizes):

        dialog = Gtk.Dialog(title="Filtrar camisetas", transient_for=self.main_window, modal=True)
        dialog.set_default_size(400, 300)

        content_area = dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        # --- ComboBox para Peñas ---
        shirt_type_label = Gtk.Label(label="Peña:")
        shirt_type_label.set_halign(Gtk.Align.START)
        content_area.append(shirt_type_label)

        self.shirt_type_combo = Gtk.ComboBoxText()
        self.shirt_type_combo.append("", "Sin filtro")
        self.shirt_type_combo.set_hexpand(True)
        for shirt_type in shirt_types:
            self.shirt_type_combo.append(str(shirt_type.id), shirt_type.name)

        if self.filter_shirt_type_id is not None:
            self.shirt_type_combo.set_active_id(str(self.filter_shirt_type_id))
        else:
            self.shirt_type_combo.set_active_id("")

        content_area.append(self.shirt_type_combo)

        # --- ComboBox para Inversores ---
        investor_label = Gtk.Label(label="Inversor:")
        investor_label.set_halign(Gtk.Align.START)
        content_area.append(investor_label)

        self.collaborator_combo = Gtk.ComboBoxText()
        self.collaborator_combo.append("", "Sin filtro")
        self.collaborator_combo.set_hexpand(True)
        for collaborator in collaborators:
            self.collaborator_combo.append(str(collaborator.id), collaborator.name)

        if self.filter_collaborator_id is not None:
            self.collaborator_combo.set_active_id(str(self.filter_collaborator_id))
        else:
            self.collaborator_combo.set_active_id("")

        content_area.append(self.collaborator_combo)

        # --- ComboBox para Tallas ---
        size_label = Gtk.Label(label="Talla:")
        size_label.set_halign(Gtk.Align.START)
        content_area.append(size_label)

        self.size_combo = Gtk.ComboBoxText()
        self.size_combo.append("", "Sin filtro")
        self.size_combo.set_hexpand(True)
        for size in sizes:
            self.size_combo.append(size, size)

        if self.filter_shirt_size is not None:
            self.size_combo.set_active_id(self.filter_shirt_size)
        else:
            self.size_combo.set_active_id("")

        content_area.append(self.size_combo)

        # --- Switch para Responsable ---
        responsible_label = Gtk.Label(label="¿Es responsable?")
        responsible_label.set_halign(Gtk.Align.START)
        content_area.append(responsible_label)

        self.responsible_switch = Gtk.Switch()
        self.responsible_switch.set_halign(Gtk.Align.START)
        self.responsible_switch.set_active(self.filter_is_responsible)

        content_area.append(self.responsible_switch)

        # --- Botones ---
        dialog.add_button("Cancelar", Gtk.ResponseType.CANCEL)
        dialog.add_button("Aplicar filtros", Gtk.ResponseType.OK)

        dialog.connect("response", self.on_filter_dialog_response)

        dialog.present()

    def on_filter_dialog_response(self, dialog, response):

        if response == Gtk.ResponseType.OK:

            self.filter_shirt_type_id = self.shirt_type_combo.get_active_id()
            self.filter_collaborator_id = self.collaborator_combo.get_active_id()
            self.filter_shirt_size = self.size_combo.get_active_id()
            self.filter_is_responsible = self.responsible_switch.get_active()

            self.presenter.restart_page()

            self.search_entry.set_text('')
            self.presenter.on_shirt_search_activate()

        dialog.destroy()

    def on_add_shirt_clicked(self, widget):
        self.presenter.on_add_shirt_clicked()

    def on_update_shirt_clicked(self, widget):
        self.presenter.on_update_shirt_clicked(self.shirt)

    def build_shirt_form(self, shirt_types, collaborators, sizes, shirt=None):
        self.shirt_dialog = Gtk.Dialog(
            title=f"{"Añadir" if shirt is None else "Editar"} camiseta",
            transient_for=self.main_window,
            modal=True
        )
        self.shirt_dialog.set_default_size(500, 600)
        self.convincing_friend = None

        content_area = self.shirt_dialog.get_content_area()
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
        cancel_button.connect("clicked", lambda btn: self.shirt_dialog.destroy())

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.connect("clicked", lambda btn: self.on_complex_form_dialog_response(self.shirt_dialog))

        # Mensaje de error arriba
        self.shirt_form_error_label = Gtk.Label()
        self.shirt_form_error_label.set_halign(Gtk.Align.START)
        self.shirt_form_error_label.get_style_context().add_class("error-label")
        self.shirt_form_error_label.set_markup('<span foreground="red"></span>')
        main_box.append(self.shirt_form_error_label)


        # INSTAGRAM
        instagram_label = Gtk.Label()
        instagram_label.set_markup('<span size="small">Instagram <span foreground="red">*</span></span>')
        instagram_label.set_halign(Gtk.Align.START)
        main_box.append(instagram_label)

        self.instagram_entry = Gtk.Entry()
        self.instagram_entry.set_placeholder_text("Instagram")
        self.instagram_entry.connect("changed", self.on_string_changed, 60, "Instagram del cliente", self.shirt_form_error_label, accept_button)
        if shirt is not None:
            self.instagram_entry.set_text(shirt.customer_instagram)

        main_box.append(self.instagram_entry)


        # PEÑA
        shirt_type_label = Gtk.Label()
        shirt_type_label.set_markup('<span size="small">Peña <span foreground="red">*</span></span>')
        shirt_type_label.set_halign(Gtk.Align.START)
        main_box.append(shirt_type_label)

        self.shirt_form_type_combo = Gtk.ComboBoxText()
        self.shirt_form_type_combo.append("", "Sin selección")

        for shirt_type in shirt_types:
            self.shirt_form_type_combo.append(str(shirt_type.id), shirt_type.name)

        if shirt is not None:
            self.shirt_form_type_combo.set_active_id(str(shirt.shirt_type_id))
        else:
            self.shirt_form_type_combo.set_active_id("")
        main_box.append(self.shirt_form_type_combo)


        # PRECIO DE COMPRA
        purchase_price_label = Gtk.Label()
        purchase_price_label.set_markup('<span size="small">Precio de compra <span foreground="red">*</span></span>')
        purchase_price_label.set_halign(Gtk.Align.START)
        main_box.append(purchase_price_label)

        self.purchase_price_entry = Gtk.Entry()
        self.purchase_price_entry.set_placeholder_text("Precio de compra")
        if shirt is not None:
            self.purchase_price_entry.set_text(str(shirt.purchase_price))

        self.purchase_price_entry.connect("changed", self.on_big_decimal_changed, "Precio de compra", self.shirt_form_error_label, accept_button)
        main_box.append(self.purchase_price_entry)


        # INVERSOR
        collaborator_label = Gtk.Label()
        collaborator_label.set_markup('<span size="small">Inversor <span foreground="red">*</span></span>')
        collaborator_label.set_halign(Gtk.Align.START)
        main_box.append(collaborator_label)

        self.shirt_form_collaborator_combo = Gtk.ComboBoxText()
        self.shirt_form_collaborator_combo.append("", "Sin selección")

        for collaborator in collaborators:
            self.shirt_form_collaborator_combo.append(str(collaborator.id), collaborator.name)

        if shirt is not None:
            self.shirt_form_collaborator_combo.set_active_id(str(shirt.investor_id))

        else:
            self.shirt_form_collaborator_combo.set_active_id("")
        main_box.append(self.shirt_form_collaborator_combo)


        # TALLA
        size_label = Gtk.Label()
        size_label.set_markup('<span size="small">Talla <span foreground="red">*</span></span>')
        size_label.set_halign(Gtk.Align.START)
        main_box.append(size_label)

        self.add_shirt_size_combo = Gtk.ComboBoxText()
        self.add_shirt_size_combo.append("", "Sin selección")

        for size in sizes:
            self.add_shirt_size_combo.append(size, size)

        if shirt is not None:
            self.add_shirt_size_combo.set_active_id(shirt.size)

        else:
            self.add_shirt_size_combo.set_active_id("")
        main_box.append(self.add_shirt_size_combo)


        # ¿AMIGO CONVENCEDOR?
        convinced_friend_label = Gtk.Label()
        convinced_friend_label.set_markup('<span size="small">Traído por:</span>')
        convinced_friend_label.set_halign(Gtk.Align.START)
        main_box.append(convinced_friend_label)

        self.convinced_friend_entry = Gtk.Entry()
        self.convinced_friend_entry.set_placeholder_text("Amigo")
        self.convinced_friend_entry.connect("changed", self.on_string_changed, 60, "Instagram del amigo", self.shirt_form_error_label, accept_button)

        if shirt is not None and shirt.convincing_friend_id is not None:
            self.convinced_friend_entry.set_text(shirt.convincing_friend_instagram)
        main_box.append(self.convinced_friend_entry)


        #¿ES RESPONSABLE?
        switch_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        switch_label = Gtk.Label(label="¿Es responsable?")
        switch_label.set_halign(Gtk.Align.START)
        self.extra_switch = Gtk.Switch()
        self.extra_switch.set_halign(Gtk.Align.END)
        self.extra_switch.set_active(shirt is not None and self.responsible is not None)
        self.extra_switch.connect("notify::active", self.on_extra_switch_toggled)

        switch_box.append(switch_label)
        switch_box.append(self.extra_switch)
        main_box.append(switch_box)


        self.extra_fields_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.extra_fields_box.set_visible(False)


        # NOMBRE DEL RESPONSABLE
        responsible_name_label = Gtk.Label()
        responsible_name_label.set_markup('<span size="small">Nombre <span foreground="red">*</span></span>')
        responsible_name_label.set_halign(Gtk.Align.START)
        self.extra_fields_box.append(responsible_name_label)

        self.responsible_name_entry = Gtk.Entry()
        self.responsible_name_entry.set_placeholder_text("Nombre del responsable")
        self.responsible_name_entry.connect("changed", self.on_string_changed, 60, "Nombre", self.shirt_form_error_label, accept_button)
        if shirt is not None and self.responsible is not None:
            self.responsible_name_entry.set_text(self.responsible.name)

        self.extra_fields_box.append(self.responsible_name_entry)


        # APELLIDO DEL RESPONSABLE
        responsible_surname_label = Gtk.Label()
        responsible_surname_label.set_markup('<span size="small">Apellido</span>')
        responsible_surname_label.set_halign(Gtk.Align.START)
        self.extra_fields_box.append(responsible_surname_label)

        self.responsible_surname_entry = Gtk.Entry()
        self.responsible_surname_entry.set_placeholder_text("Apellido del responsable")
        self.responsible_surname_entry.connect("changed", self.on_string_changed, 60, "Apellido", self.shirt_form_error_label, accept_button)

        if shirt is not None and self.responsible is not None and self.responsible.surname is not None:
            self.responsible_surname_entry.set_text(self.responsible.surname)

        self.extra_fields_box.append(self.responsible_surname_entry)


        # TELÉFONO DEL RESPONSABLE
        responsible_phone_number_label = Gtk.Label()
        responsible_phone_number_label.set_markup('<span size="small">Teléfono</span>')
        responsible_phone_number_label.set_halign(Gtk.Align.START)
        self.extra_fields_box.append(responsible_phone_number_label)

        self.responsible_phone_number_entry = Gtk.Entry()
        self.responsible_phone_number_entry.set_placeholder_text("Teléfono del responsable")
        self.responsible_phone_number_entry.connect("changed", self.on_string_changed, 15, "Teléfono", self.shirt_form_error_label, accept_button)
        if shirt is not None and self.responsible is not None and self.responsible.phone_number is not None:
            self.responsible_phone_number_entry.set_text(self.responsible.phone_number)

        self.extra_fields_box.append(self.responsible_phone_number_entry)

        main_box.append(self.extra_fields_box)

        # --- Separador ---
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        content_area.append(separator)

        # --- Caja de botones pegados abajo ---
        content_area.append(main_box)
        buttons_box.append(cancel_button)
        buttons_box.append(accept_button)

        content_area.append(buttons_box)

        self.shirt_dialog.present()

    def on_extra_switch_toggled(self, switch, _):
        """Mostrar u ocultar campos condicionales"""
        self.extra_fields_box.set_visible(switch.get_active())

    def on_validate_shirt_form(self):
        """
        Tu validación personalizada:
        Si algo no está bien, muestra el error.
        """
        error_message = ""

        if self.instagram_entry.get_text().strip() == "":
            error_message = "El instagram del cliente es obligatorio"

        # Ejemplo con otro campo:
        elif self.shirt_form_type_combo.get_active_id() == "":
            error_message = "Debes seleccionar una peña para añadir una camiseta"

        elif self.purchase_price_entry.get_text().strip() == "":
            error_message = "El precio por el que se compró la camiseta es obligatorio"

        elif self.shirt_form_collaborator_combo.get_active_id() == "":
            error_message = "Debes seleccionar el inversor que compró la camiseta"

        elif self.add_shirt_size_combo.get_active_id() == "":
            error_message = "Debes seleccionar la talla de la camiseta"

        elif self.extra_switch.get_active() == True and self.responsible_name_entry.get_text().strip() == "":
            error_message = "Si el cliente es responsable, su nombre es obligatorio"

        # Si todo está bien, oculta el mensaje
        if error_message:
            self.shirt_form_error_label.set_markup(f'<span foreground="red">{error_message}</span>')
            self.shirt_form_error_label.set_visible(True)
            return False
        else:
            self.shirt_form_error_label.set_visible(False)
            return True

    def on_complex_form_dialog_response(self, dialog):
        self.presenter.check_convinced_friend_by_instagram(self.convinced_friend_entry.get_text())

    def on_shirt_form_validated(self, option):

        if self.on_validate_shirt_form():
            instagram = self.instagram_entry.get_text()
            purchase_price = float(self.purchase_price_entry.get_text().strip())
            shirt_type_id = int(self.shirt_form_type_combo.get_active_id())
            investor_id = int(self.shirt_form_collaborator_combo.get_active_id())
            size = self.add_shirt_size_combo.get_active_id()
            convincing_friend_id = self.convincing_friend.customer_id if self.convincing_friend else None
            is_responsible = self.extra_switch.get_active()
            name = self.responsible_name_entry.get_text().strip()
            surname = self.responsible_surname_entry.get_text().strip() if not self.responsible_surname_entry.get_text().strip() == '' else None
            phone_number = self.responsible_phone_number_entry.get_text().strip() if not self.responsible_phone_number_entry.get_text().strip() == '' else None

            if option == "add":
                self.presenter.add_shirt(instagram, purchase_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, name, surname, phone_number)
            elif option == "update":
                self.presenter.update_shirt(instagram, purchase_price, self.shirt.sale_price, shirt_type_id, investor_id, size, convincing_friend_id, is_responsible, self.responsible.id if self.responsible is not None else None, name, surname, phone_number)

            self.shirt_dialog.destroy()

    # Función para inicializar el panel derecho
    def _initialize_right_panel(self):
        right_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        right_box.set_margin_top(10)
        right_box.set_margin_bottom(10)
        right_box.set_margin_start(10)
        right_box.set_margin_end(10)

        # Detalles de la camiseta
        self.shirt_details_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.shirt_details_box.set_vexpand(True)
        right_box.append(self.shirt_details_box)

        return right_box

    # Función para reiniciar la pantalla de detalles de camiseta
    def clear_shirt_details_box(self):
        while True:
            child = self.shirt_details_box.get_first_child()
            if child is None:
                break
            self.shirt_details_box.remove(child)

        self.shirt = None
        self.responsible = None

    # Función para mostrar los detalles de una camiseta
    def show_shirt_details(self, shirt, responsible):
        self.clear_shirt_details_box()

        self.shirt = shirt
        self.responsible = responsible

        self.content_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        self.content_box.set_vexpand(True)

        header_box = Gtk.CenterBox()
        header_box.set_hexpand(True)
        header_box.set_halign(Gtk.Align.FILL)

        # INSTAGRAM Y ACCIONES
        instagram_label = Gtk.Label()
        instagram_label.set_markup(f'<span size="20000"><b>{self.shirt.customer_instagram}</b></span>')
        instagram_label.set_halign(Gtk.Align.CENTER)

        actions_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

        self.update_button = Gtk.Button()
        self.update_button.set_icon_name("document-edit-symbolic")
        self.update_button.set_tooltip_text("updatear camiseta")
        self.update_button.connect("clicked", self.on_update_clicked)
        actions_box.append(self.update_button)

        self.delete_button = Gtk.Button()
        self.delete_button.set_icon_name("edit-delete-symbolic")
        self.delete_button.set_tooltip_text("Eliminar camiseta")
        self.delete_button.connect("clicked", self.on_delete_shirt_clicked)

        actions_box.append(self.delete_button)

        header_box.set_center_widget(instagram_label)
        header_box.set_end_widget(actions_box)

        self.content_box.append(header_box)

        header_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        header_separator.set_margin_top(10)
        header_separator.set_margin_bottom(10)
        self.content_box.append(header_separator)

        # TIPO DE CAMISETA Y TALLA
        shirt_type_box = Gtk.CenterBox()
        shirt_type_box.set_margin_top(5)
        shirt_type_box.set_hexpand(True)
        shirt_type_box.set_halign(Gtk.Align.FILL)

        shirt_type_center_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=30)
        shirt_type_center_content.set_halign(Gtk.Align.CENTER)

        shirt_type_link = Gtk.LinkButton.new_with_label(f"{self.shirt.shirt_type_name}", self.shirt.shirt_type_name)
        shirt_type_link.set_halign(Gtk.Align.CENTER)
        shirt_type_link.connect("activate-link", self.on_shirt_type_link_clicked)
        shirt_type_link.get_style_context().add_class("shirt-type-link")
        shirt_type_link.get_style_context().add_class("link")

        size_label = Gtk.Label()
        size_label.set_markup(f'<span size="15000">Talla {self.shirt.size}</span>')
        size_label.set_halign(Gtk.Align.CENTER)

        shirt_type_center_content.append(shirt_type_link)
        shirt_type_center_content.append(size_label)

        shirt_type_box.set_center_widget(shirt_type_center_content)

        self.content_box.append(shirt_type_box)

        # DATOS DE RESPONSABLE

        if self.responsible is not None:

            self.responsible_box = Gtk.CenterBox()
            self.responsible_box.set_margin_top(10)
            self.responsible_box.set_hexpand(True)
            self.responsible_box.set_halign(Gtk.Align.FILL)

            responsible_center_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
            responsible_center_content.set_halign(Gtk.Align.CENTER)

            name_label = Gtk.Label()
            name_label.set_markup(f'<span size="12500">{self.responsible.name}</span>')
            name_label.set_halign(Gtk.Align.CENTER)
            name_label.set_margin_end(5)

            responsible_center_content.append(name_label)

            if self.responsible.surname is not None:
                surname_label = Gtk.Label()
                surname_label.set_markup(f'<span size="12500">{self.responsible.surname}</span>')
                surname_label.set_halign(Gtk.Align.CENTER)
                surname_label.set_margin_end(15)
                responsible_center_content.append(surname_label)

            if self.responsible.phone_number is not None:
                phone_number_label = Gtk.Label()
                phone_number_label.set_markup(f'<span size="12500">{self.responsible.phone_number}</span>')
                phone_number_label.set_halign(Gtk.Align.CENTER)
                responsible_center_content.append(phone_number_label)

            self.responsible_box.set_center_widget(responsible_center_content)

            self.content_box.append(self.responsible_box)

        # DATOS DE AMIGO CONVENCEDOR

        if self.shirt.convincing_friend_id is not None:

            self.convincing_friend_box = Gtk.CenterBox()
            self.convincing_friend_box.set_margin_top(10)
            self.convincing_friend_box.set_hexpand(True)
            self.convincing_friend_box.set_halign(Gtk.Align.FILL)

            shirt_convincing_friend_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
            shirt_convincing_friend_content.set_halign(Gtk.Align.CENTER)

            convincing_friend_label = Gtk.Label()
            convincing_friend_label.set_markup(f'<span size="12500">{"Traído por: "}</span>')
            convincing_friend_label.set_halign(Gtk.Align.CENTER)

            convincing_friend_link = Gtk.LinkButton.new_with_label(f"{self.shirt.convincing_friend_instagram}", f"{self.shirt.convincing_friend_instagram}")
            convincing_friend_link.set_halign(Gtk.Align.CENTER)
            convincing_friend_link.connect("activate-link", self.on_convincing_friend_clicked)
            convincing_friend_link.get_style_context().add_class("convincing-friend-link")
            convincing_friend_link.get_style_context().add_class("link")

            shirt_convincing_friend_content.append(convincing_friend_label)
            shirt_convincing_friend_content.append(convincing_friend_link)

            self.convincing_friend_box.set_center_widget(shirt_convincing_friend_content)

            self.content_box.append(self.convincing_friend_box)


        # DATOS DE AMIGOS CONVENCIDOS
        self.convinced_friends_box = Gtk.CenterBox()
        self.convinced_friends_box.set_margin_top(10)
        self.convinced_friends_box.set_hexpand(True)
        self.convinced_friends_box.set_halign(Gtk.Align.FILL)

        convinced_friends_content = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL)
        convinced_friends_content.set_halign(Gtk.Align.CENTER)

        self.convinced_friends_button = Gtk.Button(label="Amigos traídos")
        self.convinced_friends_button.set_tooltip_text("Ver amigos convencidos")
        self.convinced_friends_button.get_style_context().add_class("custom-button")
        self.convinced_friends_button.connect("clicked", self.on_convinced_friends_clicked)

        if self.customer_convinced_friends_number == 0:
            self.convinced_friends_button.set_sensitive(False)

        convinced_friends_label = Gtk.Label()
        convinced_friends_label.set_margin_start(10)
        convinced_friends_label.set_markup(f'<span size="12500">{self.customer_convinced_friends_bought}/{self.customer_convinced_friends_number}</span>')
        convinced_friends_label.set_halign(Gtk.Align.CENTER)

        convinced_friends_content.append(self.convinced_friends_button)
        convinced_friends_content.append(convinced_friends_label)

        self.convinced_friends_box.set_center_widget(convinced_friends_content)

        self.content_box.append(self.convinced_friends_box)

        self.shirt_details_box.append(self.content_box)

        # DATOS DE COMPRA
        footer_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=20)
        footer_box.set_halign(Gtk.Align.CENTER)
        footer_box.set_valign(Gtk.Align.END)  # Anclado abajo del todo
        footer_box.set_margin_top(20)

        footer_separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        footer_separator.set_valign(Gtk.Align.END)
        footer_separator.set_margin_top(10)
        footer_separator.set_margin_bottom(10)
        self.shirt_details_box.append(footer_separator)

        self.buy_button = Gtk.Button(label=f"{self.buy_button_label}")
        self.buy_button.set_tooltip_text(f"{self.buy_tooltip_label}")

        self.presenter.apply_buy_button_styles(self.buy_button)


        self.buy_button.connect("clicked", self.on_buy_button_clicked)

        buy_labels_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=5)
        buy_labels_box.set_halign(Gtk.Align.CENTER)

        purchase_price_label = Gtk.Label(label=f"Comprada por: {self.shirt.purchase_price} €")
        sale_price_label = Gtk.Label(label=f"{self.sale_price_label}")

        buy_labels_box.append(purchase_price_label)
        buy_labels_box.append(sale_price_label)

        footer_box.append(buy_labels_box)
        footer_box.append(self.buy_button)
        self.shirt_details_box.append(footer_box)

        self.apply_css()

    # Función para updatear una camiseta
    def on_update_clicked(self, widget):
        self.presenter.on_update_shirt_clicked()

    # Función para borrar una camiseta
    def on_delete_shirt_clicked(self, widget):
        delete_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas borrar la camiseta de {self.shirt.customer_instagram}?"
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

    # Función que maneja la lógica de borrar una camiseta
    def on_delete_shirt_response(self, delete_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.presenter.on_delete_shirt(self.shirt)

        delete_dialog.close()

    # Función por si se pulsa en un amigo convencedor
    def on_convincing_friend_clicked(self, widget):
        self.presenter.on_convincing_friend_clicked(self.shirt.convincing_friend_id)

    # Función por si se pulsa en una peña
    def on_shirt_type_link_clicked(self, link_button):
        self.presenter.on_shirt_type_link_clicked(self.shirt.shirt_type_id)
        return True  # Devuelve True para evitar que intente abrir una URL real

    # Función por si se pulsa en amigos convencidos
    def on_convinced_friends_clicked(self, widget):
        self.presenter.on_shirt_convinced_friends_clicked(self.shirt)

    def build_convinced_friends(self, free_shirt_people, convinced_friends):
        self.convinced_friend_dialog = Gtk.Dialog(
            title="Amigos convencidos",
            transient_for=self.main_window,
            modal=True
        )
        self.convinced_friend_dialog.set_default_size(400, 400)

        content_area = self.convinced_friend_dialog.get_content_area()
        content_area.set_spacing(10)
        content_area.set_margin_top(20)
        content_area.set_margin_bottom(20)
        content_area.set_margin_start(20)
        content_area.set_margin_end(20)

        main_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        content_area.append(main_box)

        # Contenedor para la lista
        list_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL, spacing=10)
        list_box.set_halign(Gtk.Align.CENTER)
        list_box.set_vexpand(True)
        main_box.append(list_box)

        convinced_friends_bought = 0

        for friend in convinced_friends:
            row = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)

            convinced_friends_bought += 1 if friend.bought else 0

            # LinkButton con instagram
            link = Gtk.LinkButton.new_with_label(friend.customer_instagram, friend.customer_instagram)
            link.set_halign(Gtk.Align.CENTER)
            link.connect("activate-link", self.on_convinced_friend_clicked_from_dialog, friend)
            link.get_style_context().add_class("link")

            # CheckButton NO updateable
            check = Gtk.CheckButton()
            check.set_active(friend.bought)
            check.set_sensitive(False)
            check.set_halign(Gtk.Align.CENTER)

            row.append(link)
            row.append(check)
            list_box.append(row)

        # Separador
        separator = Gtk.Separator(orientation=Gtk.Orientation.HORIZONTAL)
        main_box.append(separator)

        # Botón Aceptar
        text_label = Gtk.Label()
        text_label.set_justify(Gtk.Justification.CENTER)
        text_label.set_halign(Gtk.Align.CENTER)
        if free_shirt_people == 0:
            text_label.set_text("No es posible conseguir gratis esta camiseta")
        elif convinced_friends_bought < free_shirt_people:
            remaining_shirts = free_shirt_people - convinced_friends_bought
            text_label.set_text(f"{"Falta" if remaining_shirts == 1 else "Faltan"} {remaining_shirts} {"compra" if remaining_shirts == 1 else "compras"} para que la camiseta sea gratis")
        else:
            text_label.set_text(f"{self.shirt.customer_instagram} tiene derecho a tener la camiseta gratis")

        main_box.append(text_label)

        accept_button = Gtk.Button(label="Aceptar")
        accept_button.set_halign(Gtk.Align.CENTER)
        accept_button.connect("clicked", lambda btn: self.convinced_friend_dialog.destroy())
        main_box.append(accept_button)

        self.convinced_friend_dialog.present()

    def on_convinced_friend_clicked_from_dialog(self, link_button, convinced_friend):
        self.convinced_friend_dialog.destroy()
        self.presenter.on_convinced_friend_clicked(convinced_friend)
        return True  # Evita que intente abrir una URL real

    # Función para comprar o deshacer la compra de una camiseta
    def on_buy_button_clicked(self, widget):

        if self.shirt.bought:
            self.undo_purchase()
        else:
            self.buy()

    # Función que maneja la compra de una camiseta
    def buy(self):
        buy_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
        )

        # Añadir botones de Cancelar y Borrar
        buy_dialog.add_buttons(
            "Cancelar", Gtk.ResponseType.CANCEL,
            "Comprar", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        buy_dialog.set_default_response(Gtk.ResponseType.OK)

        self.buy_ok_button = buy_dialog.get_widget_for_response(Gtk.ResponseType.OK)

        buy_content_area = buy_dialog.get_content_area()
        buy_content_area.set_margin_top(0)
        buy_content_area.set_margin_bottom(10)
        buy_content_area.set_margin_start(10)
        buy_content_area.set_margin_end(10)

        buy_label = Gtk.Label(halign=Gtk.Align.CENTER)
        buy_label.set_markup(f'<span size="10000"><b>Comprar camiseta de {self.shirt.customer_instagram} por:</b></span>')
        buy_label.set_valign(Gtk.Align.START)
        buy_content_area.append(buy_label)

        price_box = Gtk.Box(orientation=Gtk.Orientation.HORIZONTAL, spacing=10)
        price_box.set_halign(Gtk.Align.CENTER)

        buy_entry = Gtk.Entry()
        buy_entry.set_text(f"{self.current_price}")
        buy_entry.connect("changed", self.on_buy_entry_changed)

        buy_label = Gtk.Label(label="€")

        price_box.append(buy_entry)
        price_box.append(buy_label)

        buy_content_area.append(price_box)


        # Label de error debajo del entry
        self.buy_entry_error_label = Gtk.Label()
        self.buy_entry_error_label.set_halign(Gtk.Align.CENTER)
        self.buy_entry_error_label.get_style_context().add_class("error-label")
        self.buy_entry_error_label.set_markup("")  # Empieza vacío

        buy_content_area.append(self.buy_entry_error_label)


        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        buy_dialog.connect("response", self.on_buy_shirt_response)
        buy_dialog.present()


    # Función que maneja la lógica del precio de la camiseta
    def on_buy_entry_changed(self, widget):
        self.presenter.on_buy_entry_changed(widget.get_text())

    def on_big_decimal_changed(self, widget, attribute, error_label, okey_button_label):
        self.presenter.on_big_decimal_changed(widget.get_text(), attribute, error_label, okey_button_label)

    def on_string_changed(self, widget, attribute, max_chars, error_label, okey_button_label):
        self.presenter.on_string_changed(widget, attribute, max_chars, error_label, okey_button_label)

    # Función que maneja la respuesta de la compra de una camiseta
    def on_buy_shirt_response(self, buy_dialog, response):
        # "Delete" button
        if response == Gtk.ResponseType.OK:
            self.presenter.on_buy_shirt(self.shirt, self.current_price)

        buy_dialog.close()

    # Función para deshacer la compra una camiseta
    def undo_purchase(self):
        undo_dialog = Gtk.MessageDialog(
            transient_for=self.main_window,
            modal=True,
            message_type=Gtk.MessageType.QUESTION,
            buttons=Gtk.ButtonsType.NONE,
            text=f"¿Estás seguro de que deseas deshacer la compra de la camiseta de {self.shirt.customer_instagram}?"
        )

        # Añadir botones de Cancelar y Borrar
        undo_dialog.add_buttons(
            "No", Gtk.ResponseType.CANCEL,
            "Sí", Gtk.ResponseType.OK
        )

        # Configurar el botón por defecto
        undo_dialog.set_default_response(Gtk.ResponseType.OK)

        # Conectar la respuesta del cuadro de diálogo a la función de manejo de la respuesta
        undo_dialog.connect("response", self.on_undo_response)
        undo_dialog.present()

    # Función que maneja la respuesta de deshacer la compra de una camiseta
    def on_undo_response(self, undo_dialog, response):
        if response == Gtk.ResponseType.OK:
            self.presenter.on_undo_shirt_purchase(self.shirt)

        undo_dialog.close()

    # Función para indicar un fallo de servicio
    def on_service_failure(self, message):
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
        
        .shirt-list-row {
            margin-bottom: 4px;
        }

        .completed-row {
            background-color: #eeeeee;
            color: #888888;
        }
        
        .shirt-type-link {
            font-size: 20px;   
        }
        
        .convincing-friend-link {
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