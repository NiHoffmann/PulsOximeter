import asyncio
from bleak import BleakScanner, BleakClient
from collections import deque

WINDOW_SIZE = 5  # number of samples to average
bpm_window = deque(maxlen=WINDOW_SIZE)
co2_window = deque(maxlen=WINDOW_SIZE)

DEVICE_NAME = "Pulsoxymeter"
CHAR_UUID = "BF3FBD80063F11E59E690002A5D5C501"

async def main():
    devices = await BleakScanner.discover(timeout=5)
    target = None
    for d in devices:
        if d.name == DEVICE_NAME:
            target = d
            break
    if target is None:
        print("Device not found.")
        return

    print(f"Connecting to {target.name} ({target.address})...")
    
    async with BleakClient(target.address) as client:
        while not client.is_connected:
            pass

        for service in client.services: 
            print(f"Service UUID: {service.uuid}")
            for char in service.characteristics:
                print(f"  Characteristic UUID: {char.uuid} | Properties: {char.properties}")


        while True:
            data = await client.read_gatt_char(CHAR_UUID)
            if len(data) < 2:
                continue

            bpm = data[0]
            o2 = data[1]

            print(f"BPM: {bpm}, O2: {o2:}%")

            await asyncio.sleep(1)

asyncio.run(main())
