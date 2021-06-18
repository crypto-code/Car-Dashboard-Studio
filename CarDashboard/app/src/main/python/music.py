import os

import firebase_admin
from firebase_admin import *

import json
import pafy

from ytbe import *
from main import *

def has_favorites(id, name):
  val = db.reference("/"+id+"/"+name+"/Favorites").get() 
  return val != None and val != ""

def get_favorites(id, name):
  return [x for x in db.reference("/"+id+"/"+name+"/Favorites").get()]

def add_to_favorites(id, name, song, title):
  try:
    ref = db.reference("/"+id+"/"+name+"/Favorites")
    ref.child(song).set(title)
    return True
  except:
    return False

def remove_favorites(id, name, song):
  try:
    ref = db.reference("/"+id+"/"+name+"/Favorites")
    ref.child(song).set({})
    return True
  except:
    return False

def get_favorite_songs(id, name):
  if not has_favorites(id, name):
    return ""
  vals = get_favorites(id, name)
  songs = []
  for v in vals:
    songs.append(str(get_song(v)))
  return " / ".join(s for s in songs)

def is_favorite(id, name, songID):
  if not has_favorites(id, name):
    return False
  vals = get_favorites(id, name)
  return songID in vals
