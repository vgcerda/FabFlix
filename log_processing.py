import sys

tj_list = list()
ts_list = list()

#single instance
if len(sys.argv) == 2:
    with open(sys.argv[1], 'r') as f:
        for line in f:
            temp = line.split()
            tj_list.append(int(temp[0]))
            ts_list.append(int(temp[1]))
else:
    with open(sys.argv[1], 'r') as f1:
        for line in f1:
            temp = line.split()
            tj_list.append(int(temp[0]))
            ts_list.append(int(temp[1]))

    with open(sys.argv[2], 'r') as f2:
        for line in f2:
            temp = line.split()
            tj_list.append(int(temp[0]))
            ts_list.append(int(temp[1]))

tj = sum(tj_list) / len(tj_list)
ts = sum(ts_list) / len(ts_list)
    
print("Average Search Servlet Time(ms): {:.2f}".format((ts / 1000000)))
print("Average JDBC Time(ms):           {:.2f}".format((tj / 1000000)))