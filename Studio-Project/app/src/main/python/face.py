import face_recognition
import os
import io
import PIL.Image as Image

from firebase_admin import *
import firebase_admin

import json

privateKey = {
  "type": "service_account",
  "project_id": "car-dashboard-e3c1f",
  "private_key_id": "ec9ec7bf574674494281161e2390ec4b8a10ebfb",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDhczMCfzRCarQS\n4VLxDlJu36gBcVkL3oU+h3sksUzrCNhYE4/NxVDm0WI/p7CPn7LGZUjMcDqwyIH5\nSTRF4QH0ZmGkq5e11j+3ZAtQRmGimTAs/hXg6K5hfQH5T6WKGeC/YEx20Fp1twlI\nAQse5C5vk8xHunbciUGEuFLrVIAHYEoSQqwB6inqQGL8P608ZyUQ97akzKck1si3\nkGQDdkn4+1g4XmtNBUGR/UBhkRQHyJg/u1ERm0CGqKnakbcmB/tNdr2i9tc7V9el\nTVsjUBzMG+UaHJEzXQPOWeAoFaZGQMTiatsC/a/BzA1GqdfuAndkqNi+AcHz9G+y\nMRDKjFF/AgMBAAECggEADHfrC8nOMZgmIl8sz6EbpP8gkwfaRWJGmzkx8csOdnc0\nQ9a9HsO5YDKXTO7wwFi0Me27E3dnf4xvR8KpLe4uRf59ivZkew2A05soVwW1c6Em\nPW7F9fzYXBLmT4v8m0EoIV1pjS6juhvn2wAXxdsXyFrzssnYtgSvR3BMQbCYLhAm\nGJUmPzpDZ3Cdin+xJd3GoH+EPsaLXFFTWStzvz/Obc5CWrNNm7kID7Rdxfw7ciYF\nLpka06tqPiE9mCCH/U1m8Yz2BhhXISxcJdnhFQYIKSiRLuvfxJjLN6zVdEr22hUd\na3SulRXS1VqK5f6kAFzRUlRpZSnrYEwXA0N8ip2neQKBgQD/tnVH6nnn5yTiObkT\nhPPw52TMAym4MCp73kIkVxWYCAOc5vjYiQ31vc452ONY98tTRYURwZ/XMTSjEchb\neZME8/cCZ9n3rwmH78BxaPx1Jkums2jWaZxmfanHdnqKimOdUwgJrUZ/PSwEAFXO\nePzE0sBQ3c8rLG9ygFWXrzjbZwKBgQDhtAmmmUq1X2J85wZ3a+F+YCk8eK+IBYpi\nx6mCVZGzNGgO4GmKyEzGNvcojY0+3gVMR38kqlYtgbRR1qgVO0d+h0Yyl6XzdwL2\nGX2XRg+f74AN1/QzrJPiHIYP7TlGZC21WH+8+XGNvNsQwkgr2poCKPhoHz+VCJua\nrPdZvVaiKQKBgA7YrBSB05QYQnbz/P/aH0OkW6DQqjJscXsBm3t6puFNzc/lRRtl\nUa8r1vZa1lBLCr4J8kDmqf4XeEPje2t8MoH+HCBCoVAVFIuU6upquwWeNJQ4JGUW\neqMktp/DDDhQ8pmmwXe2XLp0dcLBtjmnop1W0x2e0zFhghL2yoFlBfsPAoGAeOpg\n+TXKhlxxgQSrvDK5fWC69sCZGUAOoVjiAQcipWUAEmg7YrRoHRC5XKpo7zM2l2T+\nQoBW5s5D8kwThDxb0vdZriT8LPCnT3zcgbxxBnfinIgDNePQ3iP11nZ6ZrGooyyk\nUnM4WHzCx7mtvIDCXUHhwrZq88bGxhi/8v2yefECgYEAmv9OoxpqU2uzlQsxb8ez\nFK1n8CSXY0YqZc0nQ3QBHT55hKhruttyRM2YRgHD/tFAc4PaamjFfAL8s0hVeVWc\nDck40yg4c3GaJzX3VJcqnOg1VluTCl1vYFsHz21rxHJ91vQJqRCu1eKBqdv9llYU\n19YilEzT/SlEbUmtYJslIc8=\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-a60n6@car-dashboard-e3c1f.iam.gserviceaccount.com",
  "client_id": "109367953203059729504",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-a60n6%40car-dashboard-e3c1f.iam.gserviceaccount.com"
}

with open(os.environ["HOME"] + "/key.json", "w") as outfile:
  json.dump(privateKey, outfile)


cred = credentials.Certificate(os.environ["HOME"] + "/key.json")
default_app = firebase_admin.initialize_app(cred, {
  'databaseURL':'https://car-dashboard-e3c1f-default-rtdb.firebaseio.com/'
})

from firebase_admin import db

ref = db.reference("/")

def check_similarity(base_enc, enc2):
  vals = []
  for f in enc2:
    mat = face_recognition.compare_faces([f], base_enc, tolerance=0.6)
    vals.append(mat[0])
  return any(x for x in vals)


def check_match_with_file(encoding, path):
  file = path.split("/")[len(path.split("/")) - 1]
  fr_image = face_recognition.load_image_file("storage/emulated/0/Pictures/Car Dashboard/" + file)
  face_encodings = face_recognition.face_encodings(fr_image)
  return check_similarity(encoding, face_encodings)

def check_match_with_image(encoding, img):
  image = Image.open(io.BytesIO(img))
  image = image.rotate(90)
  image.save("storage/emulated/0/Pictures/Car Dashboard/temp.jpg")
  fr_image = face_recognition.load_image_file("storage/emulated/0/Pictures/Car Dashboard/temp.jpg")
  face_encodings = face_recognition.face_encodings(fr_image)
  return check_similarity(encoding, face_encodings)

def check_match(img):
  enc = get_encoding_with_file("storage/emulated/0/Pictures/Car Dashboard/face.jpg")
  try:
    return check_match_with_image(enc, img)
  except:
    return -1


def get_encoding_with_file(path):
  fr_image = face_recognition.load_image_file(path)
  face_encoding = face_recognition.face_encodings(fr_image)
  if len(face_encoding) > 1 or len(face_encoding) == 0:
    raise Exception("Please show one face")
  return face_encoding[0]

def get_encoding_with_image(img):
  image = Image.open(io.BytesIO(img))
  image = image.rotate(90)
  image.save("storage/emulated/0/Pictures/Car Dashboard/temp.jpg")
  fr_image = face_recognition.load_image_file("storage/emulated/0/Pictures/Car Dashboard/temp.jpg")
  face_encoding = face_recognition.face_encodings(fr_image)
  if len(face_encoding) > 1 or len(face_encoding) == 0:
    raise Exception("Please show one face")
  return face_encoding[0]
  
def helloworld():
  return "Hello world Chaquopy!"

def getpath(path):
  file = path.split("/")[len(path.split("/")) - 1]
  return os.environ["HOME"]

def store_face(name, img):
  enc = get_encoding(img)
  ref.set({name: {"Encoding": enc.tolist()}})

def check_device(id):
  return db.reference("/" + id).get() != None

def check_face(name, img):
  enc = np.array(db.reference("/"+name+"/Encoding/").get())
  return check_match_with_file(enc, img)
