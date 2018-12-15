# io

""" The io module is run during the RPi boot sequence.
    (using /etc/rc.local) - the 'nohup' and '&' allow
    the io module to be run independently and
    unattended to in the background.
"""

import os
import time
import shutil
import logging
import threading
import subprocess
from time import sleep

src_dir = os.path.dirname(os.path.abspath(__file__))
working_dir = os.path.abspath(os.path.join(src_dir, os.pardir))

import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup([16, 20, 26], GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(21, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

logging.basicConfig(filename='RPi_IO.log', level=logging.DEBUG)

# copy USB contents to working directory
target_folder = "/home/pi/Desktop"
excluded = []


def led_ready():
    GPIO.output(26, GPIO.HIGH)  # green
    GPIO.output(16, GPIO.LOW)   # red
    GPIO.output(20, GPIO.LOW)   # blue


def led_busy():
    GPIO.output(26, GPIO.LOW)   # green
    GPIO.output(16, GPIO.HIGH)  # red
    GPIO.output(20, GPIO.LOW)   # blue


def led_running():
    GPIO.output(26, GPIO.LOW)   # green
    GPIO.output(16, GPIO.LOW)   # red
    GPIO.output(20, GPIO.HIGH)  # blue todo: blinking


def led_error():
    GPIO.output(26, GPIO.LOW)   # green
    GPIO.output(16, GPIO.HIGH)  # red todo: blinking
    GPIO.output(20, GPIO.LOW)   # blue


def get_mounted_drives():
    return [(item.split()[0].replace("├─", "").replace("└─", ""),
             item[item.find("/"):]) for item in subprocess.check_output(
        ["/bin/bash", "-c", "lsblk"]).decode("utf-8").split("\n") if "/" in item]


def identify(disk):
    command = "find /dev/disk -ls | grep /" + disk
    output = subprocess.check_output(["/bin/bash", "-c", command]).decode("utf-8")
    if "usb" in output:
        return True
    else:
        return False


def copy_usb():
    done = []
    new_paths = [dev for dev in get_mounted_drives() if not dev in done and not dev[1] == "/"]
    valid = [dev for dev in new_paths if (identify(dev[0]), dev[1].split("/")[-1] in excluded) == (True, False)]

    for item in valid:
        # todo: define a convention : all students must use the same folder structure!!!!!
        folder = item[1] + "/working_directory"
        target = target_folder + "/" + folder.split("/")[-1]

        # try:
        #     shutil.rmtree(target)
        # except FileNotFoundError:
        #     pass

        try:
            shutil.copytree(folder, target)
        except Exception as e:
            logging.warning(e)

    time.sleep(5)


def compile_javafiles():
    try:
        std_out = subprocess.check_output([working_dir + "/rsc/run.sh"])
        #  redirected std out to RPi_log.txt
        for line in std_out.splitlines():
            logging.info(line)
    except Exception as e:
        logging.warning(e)

    led_running()


def handle(pin):
    led_busy()
    copy_usb()
    # compile_javafiles()
    led_ready()


def main():
    led_ready()
    GPIO.add_event_detect(21, GPIO.BOTH, handle)

    while True:
        try:
            time.sleep(1.e6)
        except KeyboardInterrupt:
            GPIO.cleanup()


if __name__ == "__main__":
    main()
