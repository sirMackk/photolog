(ns photolog.utils
  (:require [clojure.string :as st]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell])
  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))

(def thumb-prefix "thumb_")
(def large-prefix "large_")
(def albums "albums")

(defn slugidize [string]
  (st/replace (st/trim (st/lower-case string)) #"[^a-z0-9-]+" "-"))

(defn epoch-time []
  (str (System/currentTimeMillis)))

(defn format-files [multipart]
  "Pushes a map into a vector if only a single file is uploaded."
  (if (nil? multipart)
    [nil])
  (if-not (vector? multipart)
    [multipart]
    multipart))

(defn prozess-file [file]
  (let [stamp (epoch-time)]
    (assoc file :filename (str (epoch-time) "_" (:filename file)))))

(defn save-thumbnail [{:keys [filename]} album-name]
  "Creates an empty file (File.) and uses ImageIO/write
  to write BufferedImage into that file."
  (let [path (str albums File/separator (slugidize album-name) File/separator)]
    (shell/sh "convert" "resize" "1920x" filename (str large-prefix filename) :dir path)
    (shell/sh "convert" "resize" "300x" filename (str thumb-prefix filename) :dir path)))

(defn friendly-date [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") date))
