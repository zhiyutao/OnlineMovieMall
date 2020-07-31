import sys

filename = sys.argv[1]
sum_ts = 0
num_ts = 0
sum_tj = 0
num_tj = 0
with open(filename, encoding="utf8") as f:
    for l in f:
        fields = l.strip().split(';')
        print(l)
        if "search" not in fields[1]:
            continue
        if fields[0] == "TS":
            num_ts += 1
            sum_ts += int(fields[-1][:-2]) # skip "ms"
        elif fields[0] == "TJ":
            num_tj += 1
            sum_tj += int(fields[-1][:-2])


print("TS: num: {} avg: {:.4f} ms".format(num_ts, sum_ts/num_ts if num_ts > 0 else 0))
print("TJ: num: {} avg: {:.4f} ms".format(num_tj, sum_tj/num_tj if num_tj > 0 else 0))
