from ytmusicapi import YTMusic
import json
import os
import pafy
import time

header = {
    "accept": "*/*",
    "accept-encoding": "gzip, deflate",
    "accept-language": "en-US,en;q=0.9",
    "authorization": "SAPISIDHASH 1622014473_839dba6ab5425521abb8fbf6dc9becb713ae0eb3",
    "content-encoding": "gzip",
    "content-type": "application/json",
    "cookie": "VISITOR_INFO1_LIVE=y3b_H9LIr00; _gcl_au=1.1.1297668704.1622013931; PREF=f4=4000000&tz=Asia.Dubai&f6=40000001&volume=100; YSC=veFlMO3-EDM; SID=9wcMBG2In7A-hpBLd5KAw5p7uf2g8rnJbnA7KdLxfza5smQm0yjzkLIrNAEoqx4KV_V92w.; __Secure-3PSID=9wcMBG2In7A-hpBLd5KAw5p7uf2g8rnJbnA7KdLxfza5smQmSSUDfbA5RhkqOi_40FnGkw.; HSID=AW6XvNaTjhDpHip_t; SSID=Ak9_05UxJ8IRp9d7U; APISID=f7HpUAy3JTAgOl92/A-XoTSAtkP9sVHeUK; SAPISID=8GLZtlIL5auUc4zL/AwLag43aSFvtQGpNC; __Secure-3PAPISID=8GLZtlIL5auUc4zL/AwLag43aSFvtQGpNC; LOGIN_INFO=AFmmF2swRAIgdrU18WpRJJb49YAUTTwKKKl2MnYW8CFJ5JnrT8x-WBwCIHTQ3WdYO1jdjWg4u5zWq5fb7EqLIDvDH5jI7Lx4xYnn:QUQ3MjNmeXEzbmR4ZDBac0tmNm55VU4tcWgxQ1VGUnNVeHpMVlhlYlBjNjlIQjc2NWpkdHdZZ1dDSHh1eVdxSmRZWVRycDlOcktoeElobVliY1hQUE9iX0ZRY2JLaFhWYnVRYUJDcHlTMHMtSzNYNGJZMG1MU2RkVmdRTmo5VUVjT3JZbV9NSVdSbWRoN0k0V2JLUU02cTVkTFU1amRMb2p3; SIDCC=AJi4QfGMYwSFFTW8G0Qc5gJDm8Rb6MjBQAuy5wwvHFiaBvylozs3e_5G3ZRIfBHIdKX8czUG; __Secure-3PSIDCC=AJi4QfHW9LH_5t8md-tpurPNPe3w3M027Ksb633WU73qV10P9hiFrXcCcqDp-qGrvEwqqNic",
    "origin": "https://music.youtube.com",
    "referer": "https://music.youtube.com/",
    "sec-ch-ua": "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Google Chrome\";v=\"90\"",
    "sec-ch-ua-mobile": "?0",
    "sec-fetch-dest": "empty",
    "sec-fetch-mode": "cors",
    "sec-fetch-site": "same-origin",
    "user-agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36",
    "x-client-data": "CIy2yQEIo7bJAQjEtskBCKmdygEI+MfKAQj7z8oBCJ/3ygEIqJ3LAQiioMsBCN3yywEIp/PLAQ==",
    "x-goog-authuser": "0",
    "x-goog-pageid": "undefined",
    "x-goog-visitor-id": "Cgt5M2JfSDlMSXIwMCiI9LeFBg%3D%3D",
    "x-origin": "https://music.youtube.com",
    "x-youtube-ad-signals": "dt=1622014472984&flash=0&frm&u_tz=240&u_his=7&u_java&u_h=1120&u_w=1792&u_ah=1009&u_aw=1792&u_cd=30&u_nplug=3&u_nmime=4&bc=31&bih=887&biw=1253&brdim=0%2C25%2C0%2C25%2C1792%2C25%2C1792%2C998%2C1265%2C887&vis=1&wgl=true&ca_type=image&bid=ANyPxKpgvpfwA0KNi7VRTPaXZwl3XNJbra4eV5RyQw5RTR0fzsK2w0Bw3vzMBaxvEfNnrX6jZiJQcIwf7MjVq4BYHraBougsqw",
    "x-youtube-client-name": "67",
    "x-youtube-client-version": "1.20210519.00.00",
    "x-youtube-device": "cbr=Chrome&cbrand=apple&cbrver=90.0.4430.212&ceng=WebKit&cengver=537.36&cos=Macintosh&cosver=10_15_7&cplatform=DESKTOP&cyear=2013",
    "x-youtube-identity-token": "QUFFLUhqa3NYUF9wWFZyb005V3JsZU95VGZ3bndQRkFNd3w=",
    "x-youtube-page-cl": "374633197",
    "x-youtube-page-label": "youtube.music.web.client_20210519_00_RC00",
    "x-youtube-time-zone": "Asia/Dubai",
    "x-youtube-utc-offset": "240"
}

with open(os.environ["HOME"] + "/header_auth.json", "w") as outfile:
  json.dump(header, outfile)

ytmusic = YTMusic(os.environ["HOME"] + "/header_auth.json")


def get_songs(name):
  results = ytmusic.search(name)
  songs = []
  for r in results:
    try:
      tmp = [int(x) for x in r["duration"].split(":")]
      songs.append(str({"title": r["title"], "id": r["videoId"], "url": get_url(r["videoId"]),
      "artists": ", ".join(x["name"] for x in r["artists"]),
      "duration": "{:02d}:{:02d}".format(tmp[0], tmp[1]), "thumbnail": r["thumbnails"][0]["url"]}))
    except:
      continue
  return " / ".join(s for s in songs)

def get_url(songID):
  video = pafy.new("https://youtube.com/watch?v="+songID)
  return video.getbest().url

def get_song(songID):
  details = ytmusic.get_song(songID)['videoDetails']
  return {"title": details["title"], "id": songID, "url":get_url(songID),
  "artists": details["author"], "duration": sec_to_time(details["lengthSeconds"]),
  "thumbnail": details["thumbnail"]["thumbnails"][0]["url"]}

def sec_to_time(sec):
  ty_res = time.gmtime(int(sec))
  if int(sec) >= 3600:
    return time.strftime("%H:%M:%S",ty_res)
  else:
    return time.strftime("%M:%S",ty_res)


