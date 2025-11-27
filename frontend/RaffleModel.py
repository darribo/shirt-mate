import json
from decimal import Decimal

import requests

SERVER_URL = "http://127.0.0.1:8080/raffles"

def _camel_to_snake(name):
    import re
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

class RaffleException(Exception):
    def __init__(self, msg: str):
        super().__init__(msg)

class Raffle:
    def __init__(self, data=None):
        self.id = None
        self.participation_price = None
        self.description = None
        self.shirt_type_id = None
        self.battle_pass = []

        if data is not None:
            for key, value in data.items():
                snake_key = _camel_to_snake(key)
                if snake_key == "battle_pass" and isinstance(value, list):
                    self.battle_pass = [Level(item) for item in value]
                    self.battle_pass.sort(key=lambda lvl: lvl.id if lvl.id is not None else float('inf'))
                else:
                    setattr(self, snake_key, value)


class Level:
    def __init__(self, data=None):
        self.id = None
        self.level_description = None
        self.price = None
        self.necessary_participants = None
        self.winner_id = None
        self.winner_instagram = None
        self.raffle_id = None

        if data is not None:
            for key, value in data.items():
                # Convierte camelCase a snake_case
                snake_key = _camel_to_snake(key)
                setattr(self, snake_key, value)


class RaffleModel:
    def __init__(self):
        pass

    def add_raffle(self, participation_price: Decimal, description: str, shirt_type_id: int) -> Raffle:

        try:
            url = f"{SERVER_URL}/raffle"

            data = {
                "participationPrice": participation_price,
                "description": description,
                "shirtTypeId": shirt_type_id,
            }

            response = requests.post(url, json=data)

            if response.ok:
                return Raffle(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def update_raffle(self, id: int, participation_price: Decimal, description: str, shirt_type_id: int) -> Raffle:

        try:
            url = f"{SERVER_URL}/raffle/{id}"

            data = {
                "participationPrice": participation_price,
                "description": description,
                "shirtTypeId": shirt_type_id,
            }

            response = requests.put(url, json=data)

            if response.ok:
                return Raffle(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def get_raffle(self, id: int) -> Raffle:

        try:
            url = f"{SERVER_URL}/raffle/{id}"

            response = requests.get(url)

            if response.ok:
                return Raffle(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")

    def get_raffle_by_shirt_type_id(self, shirt_type_id: int) -> Raffle:

        try:
            url = f"{SERVER_URL}/raffle/getByShirtTypeId?shirtTypeId={shirt_type_id}"

            response = requests.get(url)

            if response.ok:
                return Raffle(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def delete_raffle(self, id: int) -> None:

        try:
            url = f"{SERVER_URL}/raffle/{id}"

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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def get_raffle_participants_number(self, id: int) -> int:

        try:
            url = f"{SERVER_URL}/raffle/participantsNumber/{id}"

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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def get_raffle_price(self, id: int) -> Decimal:

        try:
            url = f"{SERVER_URL}/raffle/price/{id}"

            response = requests.get(url)

            if response.ok:
                return Decimal(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def exists_by_shirt_type_id(self, shirt_type_id: int) -> bool:

        try:
            url = f"{SERVER_URL}/raffle/existsByShirtTypeId?shirtTypeId={shirt_type_id}"

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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def add_level(self, level_description: str, price: Decimal, necessary_participants: int, raffle_id: int) -> Level:

        try:
            url = f"{SERVER_URL}/level"

            data = {
                "levelDescription": level_description,
                "price": price,
                "necessaryParticipants": necessary_participants,
                "raffleId": raffle_id
            }

            response = requests.post(url, json=data)

            if response.ok:
                return Level(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def update_level(self, id: int, level_description: str, price: Decimal, necessary_participants: int) -> Level:

        try:
            url = f"{SERVER_URL}/level/{id}"

            data = {
                "levelDescription": level_description,
                "price": price,
                "necessaryParticipants": necessary_participants,
            }

            response = requests.put(url, json=data)

            if response.ok:
                return Level(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")



    def get_level(self, id: int) -> Level:

        try:
            url = f"{SERVER_URL}/level/{id}"

            response = requests.get(url)

            if response.ok:
                return Level(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def delete_level(self, id: int) -> None:

        try:
            url = f"{SERVER_URL}/level/{id}"

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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def level_raised(self, id: int) -> bool:

        try:
            url = f"{SERVER_URL}/level/raised/{id}"

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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")


    def play_level(self, id: int) -> Level:

        try:
            url = f"{SERVER_URL}/level/play/{id}"

            response = requests.post(url)

            if response.ok:
                return Level(response.json())
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

                raise RaffleException(message)

        except Exception as e:
            if isinstance(e, RaffleException):
                raise e
            else:
                raise RaffleException("Error desconocido")