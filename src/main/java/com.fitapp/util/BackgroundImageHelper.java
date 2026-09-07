package com.fitapp.util;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public final class BackgroundImageHelper {

    private BackgroundImageHelper() {
        // Utility-Klasse darf nicht instanziiert werden.
    }

    /**
     * Passt ein ImageView so an, dass es den gesamten StackPane ausfüllt.
     *
     * Verhalten:
     * - Bild füllt immer die komplette Fläche
     * - Seitenverhältnis bleibt erhalten
     * - Keine Verzerrung
     * - Überstehende Bildbereiche werden abgeschnitten
     */
    public static void setup(
            StackPane pane,
            ImageView imageView
    ) {

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Rechteck zum korrekten Abschneiden des Bildes
        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());

        pane.setClip(clip);

        // Bei Änderung der Fenstergröße Bild neu berechnen
        pane.widthProperty().addListener(
                (obs, oldValue, newValue) ->
                        update(pane, imageView)
        );

        pane.heightProperty().addListener(
                (obs, oldValue, newValue) ->
                        update(pane, imageView)
        );

        update(pane, imageView);
    }

    private static void update(
            StackPane pane,
            ImageView imageView
    ) {

        if (imageView.getImage() == null) {
            return;
        }

        double paneWidth = pane.getWidth();
        double paneHeight = pane.getHeight();

        double imageWidth = imageView.getImage().getWidth();
        double imageHeight = imageView.getImage().getHeight();

        if (paneWidth <= 0 ||
                paneHeight <= 0 ||
                imageWidth <= 0 ||
                imageHeight <= 0) {
            return;
        }

        /*
         * Der größere Skalierungsfaktor sorgt dafür,
         * dass das Bild die komplette Fläche bedeckt.
         */
        double scale = Math.max(
                paneWidth / imageWidth,
                paneHeight / imageHeight
        );

        imageView.setFitWidth(imageWidth * scale);
        imageView.setFitHeight(imageHeight * scale);
    }
}
