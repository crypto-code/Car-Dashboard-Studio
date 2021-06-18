import requests
import haversine as hs
from main import *
import json

def get_search_results(query, lat, lon):
  params = {"q": query, "lat": lat, "lon": lon, "lang": "en"}
  r = requests.get("https://photon.komoot.io/api/", params)
  res = r.json()["features"]
  coord = (float(lat), float(lon))
  sorted_res = sorted(res, key=lambda k: hs.haversine((float(k['geometry']['coordinates'][1]), float(k['geometry']['coordinates'][0])), coord))
  results = []
  for r in sorted_res:
    try:
      dist = hs.haversine((float(r['geometry']['coordinates'][1]), float(r['geometry']['coordinates'][0])), coord)
      if dist < 1:
        dist = round(dist * 1000)
        dist = str(dist) + "m"
      else:
        dist = str(round(dist, 2)) + "km"
      
      results.append({"id": r['properties']['osm_id'], "name": r['properties']['name'].replace(" / ", " "), "lat": float(r['geometry']['coordinates'][1]), "lon": float(r['geometry']['coordinates'][0]), "place": (r['properties']['state'] + ", " + r['properties']['country']).replace(" / ", " "), "distance": dist})
    except:
      continue
  return " / ".join(str(x) for x in results)


def add_to_favorites(id, name, locId, info):
  ref = db.reference("/"+id+"/"+name+"/Locations")
  details = json.loads(info)
  del details["distance"]
  ref.child(locId).set(details)
  return True


def has_favorites(id, name):
  val = db.reference("/"+id+"/"+name+"/Locations").get() 
  return val != None and val != ""

def get_favorites(id, name):
  return [x for y, x in db.reference("/"+id+"/"+name+"/Locations").get().items()]


def remove_favorites(id, name, locId):
  try:
    ref = db.reference("/"+id+"/"+name+"/Locations")
    ref.child(locId).set({})
    return True
  except:
    return False

def is_favorite(id, name, locId):
  if not has_favorites(id, name):
    return False
  vals = [x for x in db.reference("/"+id+"/"+name+"/Locations").get()]
  return locId in vals

def get_favorite_locs(id, name, lat, lon):
  if not has_favorites(id, name):
    return ""
  vals = get_favorites(id, name)
  locs = []
  coord = (float(lat), float(lon))
  for v in vals:
    dist = hs.haversine((float(v['lat']), float(v['lon'])), coord)
    if dist < 1:
      dist = round(dist * 1000)
      dist = str(dist) + "m"
    else:
      dist = str(round(dist, 2)) + "km"
    v['distance'] = dist    
    locs.append(str(v))
  return " / ".join(l for l in locs)

