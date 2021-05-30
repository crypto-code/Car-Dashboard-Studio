from main import *


def get_all_users(id):
  vals = ref.child(id).get()
  users = []
  for name, info in vals.items():
    users.append(str({"Name": name, "PIN": str(info["PIN"]), "is_admin": info["is_admin"]}))
  return " / ".join(x for x in users)

def remove_user(id, name):
  try:
    ref = db.reference("/" + id)
    ref.child(name).set({})
    return True
  except:
    return False

def check_if_present(id, name):
  vals = ref.child(id).get()
  return name in [n for n in vals]

def get_admin_status(id, name):
  vals = ref.child(id).get()
  return vals[name]["is_admin"]

def add_user_with_face(id, name, pin, img, is_admin):
  try:
    ref = db.reference("/" + id)
    enc = get_encoding_with_image(img)
    ref.child(name).set({"PIN": pin, "Encoding": enc.tolist(), "is_admin": str(is_admin)})
    return True
  except:
    return False

def add_user_without_face(id, name, pin, is_admin):
  try:
    ref = db.reference("/" + id)
    ref.child(name).set({"PIN": pin, "Encoding": "", "is_admin": str(is_admin)})
    return True
  except:
    return False

def toggle_admin_status(id, name):
  try:
    ref = db.reference("/" + id + "/" + name)
    if ref.child("is_admin").get() == "True":
      ref.child("is_admin").set("False")
    else: 
      ref.child("is_admin").set("True")
    return True
  except:
    return False
