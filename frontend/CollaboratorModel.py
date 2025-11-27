import json
from decimal import Decimal

import requests

SERVER_URL = "http://127.0.0.1:8080/collaborators"

def _camel_to_snake(name):
    import re
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

class CollaboratorException(Exception):
    def __init__(self, msg: str):
        super().__init__(msg)

class Collaborator:
    def __init__(self, data=None):
        self.id = None
        self.name = None
        self.profit_percentage = None

        if data is not None:
            for key, value in data.items():
                # Convierte camelCase a snake_case
                snake_key = _camel_to_snake(key)
                setattr(self, snake_key, value)


class CollaboratorModel:
    def __init__(self):
        pass

    def add_collaborator(self, name: str, profit_percentage: Decimal) -> Collaborator:

        try:
            url = f"{SERVER_URL}/collaborator"

            data = {
                "name": name,
                "profitPercentage": profit_percentage,
            }

            response = requests.post(url, json=data)

            if response.ok:
                return Collaborator(response.json())
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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def update_collaborator(self, id: int, name: str, profit_percentage: Decimal) -> Collaborator:

        try:
            url = f"{SERVER_URL}/collaborator/{id}"

            data = {
                "name": name,
                "profitPercentage": profit_percentage,
            }

            response = requests.put(url, json=data)

            if response.ok:
                return Collaborator(response.json())
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

                raise CollaboratorException(message)

        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_collaborator(self, id: int) -> Collaborator:

        try:
            url = f"{SERVER_URL}/collaborator/{id}"

            response = requests.get(url)

            if response.ok:
                return Collaborator(response.json())
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

                raise CollaboratorException(message)

        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_collaborators(self, name: str, page: int) -> (list[Collaborator], bool):

        try:
            url = f"{SERVER_URL}/collaborators"

            if name or page:
                url += "?"
            if name:
                url += f"name={name}&"
            if page:
                url += f"page={page}"

            response = requests.get(url)

            if response.ok:

                block = response.json()
                collaborators = []
                for collaborator in block["items"]:
                    collaborators.append(Collaborator(collaborator))
                exist_more_items = block["existMoreItems"]
                return collaborators, exist_more_items

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

                raise CollaboratorException(message)

        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_all_collaborators(self):

        try:
            url = f"{SERVER_URL}/allCollaborators"

            response = requests.get(url)

            collaborators = []

            if response.ok:
                for collaborator in response.json():
                    collaborators.append(Collaborator(collaborator))

                return collaborators
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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def delete_collaborator(self, id: int) -> None:

        try:
            url = f"{SERVER_URL}/collaborator/{id}"

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

                raise CollaboratorException(message)

        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_number_of_bought_shirts(self, id: int) -> int:

        try:
            url = f"{SERVER_URL}/collaborators/numberOfBoughtShirts/{id}"

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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")

    def get_investment(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/collaborators/investment/{id}"

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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_profit(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/collaborators/profit/{id}"
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

                raise CollaboratorException(message)

        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_valid_percentage(self, id: int, percentage: Decimal) -> bool:

        try:
            url = f"{SERVER_URL}/collaborators/validPercentage?"

            if id is not None:
                url += f"id={id}&"

            url += f"percentage={percentage}"

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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")


    def get_returned_investment(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/collaborators/returnedInvestment/{id}"

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

                raise CollaboratorException(message)
        except Exception as e:
            if isinstance(e, CollaboratorException):
                raise e
            else:
                raise CollaboratorException("Error desconocido")