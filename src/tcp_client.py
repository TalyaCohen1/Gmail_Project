import socket
import sys

if len(sys.argv) != 3:
    print("Usage: python tcp_client.py <server_ip> <port>")
    sys.exit(1)

server_ip = sys.argv[1]
server_port = int(sys.argv[2])

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((server_ip, server_port))

msg = input()
while not msg == 'quit':
    s.send(bytes(msg + '\n', 'utf-8'))
    data = s.recv(4096)
    print(data.decode('utf-8').strip())
    msg = input()

s.close()