import sys
from telegram.ext import *


def main(chat, alert):
    token = "1794376012:AAFqfMrJD-axHouu8feNxbaixDgP9i4M7LI"
    updater = Updater(token)

    dispatcher = updater.dispatcher

    dispatcher.bot.send_message(chat, alert)


if __name__ == "__main__":
    # print(sys.argv)
    main(sys.argv[1], sys.argv[2])


