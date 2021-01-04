package clue.model

class InvalidCardException(card : String) : Exception("Not a valid card: $card")