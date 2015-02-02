(ns photolog.utils
  (:require [clojure.string :as st]
            [clojure.java.io :as io])
  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))

(def thumb-prefix "thumb_")
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

(defn scale [img ratio width height]
  "Creates an AffineTransform Scale object using the ration, then, feeds that
  to the AffineTransfromOp constructor. Finally, the filter method of transform-op
  is called with the img BufferedImage as src and a new BufferedImage object
  as the dst."
  (let [scale (AffineTransform/getScaleInstance (double ratio) (double ratio))
        transform-op (AffineTransformOp. scale AffineTransformOp/TYPE_BILINEAR)]
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file]
  "Creates a BufferedImage object using ImageIO and sets the correct ratios"
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ratio 0.5]
    (scale img ratio (int (* img-width ratio)) (int (* img-height ratio)))))

(defn save-thumbnail [{:keys [filename]} album-name]
  "Creates an empty file (File.) and uses ImageIO/write
  to write BufferedImage into that file."
  (let [path (str albums File/separator (slugidize album-name) File/separator)]
    (with-open [output_image (io/input-stream (str path filename))]
      (ImageIO/write
        (scale-image output_image)
        "jpeg"
        (File. (str path thumb-prefix filename))))))

(defn friendly-date [date]
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") date))
