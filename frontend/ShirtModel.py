import json
from decimal import Decimal

import requests

SERVER_URL = "http://127.0.0.1:8080/shirts"


def camel_to_snake(name):
    import re
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

class ShirtException(Exception):
    def __init__(self, msg: str):
        super().__init__(msg)

class ShirtType:
    def __init__(self, data=None):
        self.id = None
        self.name = None
        self.image = None
        self.base_sales_price = None
        self.description = None
        self.free_shirt_people = None
        self.responsible_id = None

        if data is not None:
            for key, value in data.items():
                # Convierte camelCase a snake_case
                snake_key = camel_to_snake(key)
                setattr(self, snake_key, value)


class Shirt:
    def __init__(self, data=None):
        # Siempre inicializa todos los atributos
        self.id = None
        self.purchase_price = None
        self.sale_price = None
        self.size = None
        self.customer_id = None
        self.customer_instagram = None
        self.convincing_friend_id = None
        self.convincing_friend_instagram = None
        self.investor_id = None
        self.shirt_type_id = None
        self.shirt_type_name = None
        self.bought = None

        if data is not None:
            for key, value in data.items():
                snake_key = camel_to_snake(key)
                setattr(self, snake_key, value)


class Responsible:
    def __init__(self, data=None):

        self.id = None
        self.name = None
        self.surname = None
        self.phone_number = None
        self.customer_id = None

        if data is not None:
            for key, value in data.items():
                # Convierte camelCase a snake_case
                snake_key = camel_to_snake(key)
                setattr(self, snake_key, value)


class ShirtModel:
    def __init__(self):
        pass

    def add_shirt_type(self, name: str, image: str, base_sales_price: float, description: str, free_shirt_people: int) -> ShirtType:

        try:
            url = f"{SERVER_URL}/shirtType"
            shirt_type_data = {
                "name": name,
                "image": image,
                "baseSalesPrice": base_sales_price,
                "description": description,
                "freeShirtPeople": free_shirt_people,
            }

            response = requests.post(url, json=shirt_type_data)

            if response.ok:
                return ShirtType(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def update_shirt_type(self, id: int, name: str, image: str, base_sales_price: float, description: str, free_shirt_people: int, responsible_id: int) -> ShirtType:

        try:
            url = f"{SERVER_URL}/shirtType/{id}"
            shirt_type_data = {
                "name": name,
                "image": image,
                "baseSalesPrice": base_sales_price,
                "description": description,
                "freeShirtPeople": free_shirt_people,
                "responsibleId": responsible_id,
            }

            response = requests.put(url, json=shirt_type_data)

            if response.ok:
                return ShirtType(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_shirt_type(self, id: int) -> ShirtType:

        try:
            url = f"{SERVER_URL}/shirtType/{id}"
            response = requests.get(url)

            if response.ok:
                return ShirtType(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def get_shirt_types(self, name: str, page: int) -> (list[ShirtType], bool):

        try:
            url = f"{SERVER_URL}/shirtTypes"

            if name or page:
                url += "?"

            if name:
                url += f"name={name}&"
            if page:
                url += f"page={page}"

            response = requests.get(url)

            if response.ok:

                block = response.json()
                shirt_types = []

                for shirt_type in block["items"]:
                    shirt_types.append(ShirtType(shirt_type))

                exist_more_items = block["existMoreItems"]
                return shirt_types, exist_more_items
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_all_shirt_types(self):

        try:
            url = f"{SERVER_URL}/allShirtTypes"

            response = requests.get(url)

            if response.ok:

                shirt_types = []

                for shirt_type in response.json():
                    shirt_types.append(ShirtType(shirt_type))

                return shirt_types
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def delete_shirt_type(self, id: int) -> None:

        try:
            url = f"{SERVER_URL}/shirtType/{id}"
            response = requests.delete(url)
            if not response.ok:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def add_shirt(self, instagram: str, purchase_price: float, shirt_type_id: int, investor_id: int, size: str, convincing_friend_id: int, is_responsible: bool, name: str, surname: str, phone_number: str) -> Shirt:

        try:
            url = f"{SERVER_URL}/shirt"
            shirt_data = {
                "instagram": instagram,
                "purchasePrice": purchase_price,
                "shirtTypeId": shirt_type_id,
                "investorId": investor_id,
                "size": size,
                "convincingFriendId": convincing_friend_id,
                "isResponsible": is_responsible,
                "name": name,
                "surname": surname,
                "phoneNumber": phone_number
            }

            response = requests.post(url, json=shirt_data)

            if response.ok:
                return Shirt(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def update_shirt(self, id: int, instagram: str, purchase_price: Decimal, sale_price: Decimal, shirt_type_id: int, investor_id: int, size: str, convincing_friend_id: int, is_responsible: bool, responsible_id: int, name: str, surname: str, phone_number: str) -> Shirt:

        try:
            url = f"{SERVER_URL}/shirt/{id}"
            shirt_data = {
                "instagram": instagram,
                "purchasePrice": purchase_price,
                "salePrice": sale_price,
                "shirtTypeId": shirt_type_id,
                "investorId": investor_id,
                "size": size,
                "convincingFriendId": convincing_friend_id,
                "isResponsible": is_responsible,
                "responsibleId" : responsible_id,
                "name": name,
                "surname": surname,
                "phoneNumber": phone_number
            }

            response = requests.put(url, json=shirt_data)

            if response.ok:
                return Shirt(response.json())
            else:

                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_shirt(self, id: int) -> Shirt:

        try:
            url = f"{SERVER_URL}/shirt/{id}"
            response = requests.get(url)

            if response.ok:
                return Shirt(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def delete_shirt(self, id: int) -> None:

        try:
            url = f"{SERVER_URL}/shirt/{id}"
            response = requests.delete(url)

            if not response.ok:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def search_shirts(self, keywords: str, shirt_type_id: int, investor_id: int, shirt_size: str, is_responsible: bool, page: int) -> (list[Shirt], bool):

        try:
            url = f"{SERVER_URL}/shirts"

            if keywords or shirt_type_id or investor_id or shirt_size or is_responsible or page:
                url += "?"

            if keywords:
                url += f"keywords={keywords}&"

            if shirt_type_id:
                url += f"shirtTypeId={shirt_type_id}&"

            if investor_id:
                url += f"investorId={investor_id}&"

            if shirt_size:
                url += f"shirtSize={shirt_size}&"

            if is_responsible:
                url += f"isResponsible={is_responsible}&"

            if page:
                url += f"page={page}"

            response = requests.get(url)

            if response.ok:

                block = response.json()
                shirts = []

                for shirt in block["items"]:
                    shirts.append(Shirt(shirt))

                exist_more_items = block["existMoreItems"]
                return shirts, exist_more_items

            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def shirt_type_revenue(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/shirtType/shirtTypeRevenue/{id}"
            response = requests.get(url)
            if response.ok:
                value = Decimal(response.json()).quantize(Decimal("0.01"))
                return value
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def customers_number(self, id: int) -> int:

        try:
            url = f"{SERVER_URL}/shirtType/customersNumber/{id}"
            response = requests.get(url)
            if response.ok:
                return int(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def convinced_friends(self, id: int) -> list[Shirt]:

        try:
            url = f"{SERVER_URL}/shirts/convincedFriends/{id}"
            response = requests.get(url)

            if response.ok:
                convinced_friends = []
                for friend in response.json():
                    convinced_friends.append(Shirt(friend))
                return convinced_friends
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def convinced_friends_number(self, id: int) -> int:

        try:
            url = f"{SERVER_URL}/shirts/convincedFriendsNumber/{id}"
            response = requests.get(url)

            if response.ok:
                return int(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def convinced_friends_paid(self, id: int) -> int:

        try:
            url = f"{SERVER_URL}/shirts/convincedFriendsPaid/{id}"
            response = requests.get(url)

            if response.ok:
                return int(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def is_free_shirt(self, id: int) -> bool:

        try:
            url = f"{SERVER_URL}/shirts/isFree/{id}"
            response = requests.get(url)

            if response.ok:
                return bool(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_current_price(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/shirts/currentPrice/{id}"
            response = requests.get(url)

            if response.ok:
                value = Decimal(response.json()).quantize(Decimal("0.01"))
                return value
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def buy(self, id: int, salePrice: Decimal) -> Shirt:

        try:
            url = f"{SERVER_URL}/shirts/buy/{id}"

            url += f"?salePrice={salePrice}"

            response = requests.post(url)
            if response.ok:
                return Shirt(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_total_profit(self) -> Decimal:

        try:
            url = f"{SERVER_URL}/shirts/totalProfit"
            response = requests.get(url)
            if response.ok:
                value = Decimal(response.json()).quantize(Decimal("0.01"))
                return value
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_responsible_by_customer_id(self, customer_id: int) -> Responsible:

        try:
            url = f"{SERVER_URL}/shirts/responsibleByCustomerId/{customer_id}"
            response = requests.get(url)

            if response.ok:
                return Responsible(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")
        
    
    def get_responsible(self, id: int) -> Responsible:
        
        try:
            url = f"{SERVER_URL}/shirts/responsible/{id}"
            response = requests.get(url)
            
            if response.ok:
                return Responsible(response.json())
            else:
                error_content = json.loads(response.content)
            if "globalError" in error_content:
                message = error_content["globalError"]
            elif "fieldErrors" in error_content:
                # Si hay varios errores de campo
                messages = [error["message"] for error in error_content["fieldErrors"]]
                message = "\n".join(messages)
            else:
                message = "Error desconocido"
    
            raise ShirtException(message)
        
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def is_responsible(self, customer_id: int) -> bool:

        try:
            url = f"{SERVER_URL}/shirts/isResponsible/{customer_id}"

            response = requests.get(url)

            if response.ok:
                return bool(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")

    def get_shirt_by_customer_id(self, customer_id: int) -> Shirt:

        try:
            url = f"{SERVER_URL}/shirts/shirtByCustomer/{customer_id}"

            response = requests.get(url)

            if response.ok:
                return Shirt(response.json())
            else:
                error_content = json.loads(response.content)
                # Extrae el mensaje según clave
                if "globalError" in error_content:
                    message = error_content["globalError"]
                elif "fieldErrors" in error_content:
                    # Si hay varios errores de campo
                    messages = [error["message"] for error in error_content["fieldErrors"]]
                    message = "\n".join(messages)
                else:
                    message = "Error desconocido"

                raise ShirtException(message)
        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")


    def get_shirt_by_instagram(self, instagram: str) -> (bool, Shirt):

        try:
            url = f"{SERVER_URL}/shirts/getByInstagram?instagram={instagram}"

            response = requests.get(url)

            if response.ok:
                return True, Shirt(response.json())
            else:
                return False, None

        except Exception as e:
            if isinstance(e, ShirtException):
                raise e
            else:
                raise ShirtException("Error desconocido")