# -java_
This project implements a JavaFX-based client application for image processing and basic image classification using a client–server architecture. The client communicates with a locally hosted image-processing API over TCP sockets using HTTP-style POST requests, transmitting images encoded in Base64 format.

Users can select images, apply processing operations, extract visual features, and perform simple classification based on extracted feature values.

Key Features:

Graphical User Interface

Image selection and preview

Display of processed images

Server responses and status feedback

Client–Server Communication

Socket-based communication with a local API (localhost:5000)

Custom HTTP-style request formation

Base64 image encoding and decoding

Image Processing

Grayscale conversion

Morphological erosion

Morphological dilation

Feature Extraction and Classification

FAST feature extraction

Feature-based threshold classification

Visualisation of detected features

Technical Stack




GUI Framework: JavaFX

Networking: Java Sockets

Data Encoding: Base64

Architecture: Client–Server

Purpose

The project demonstrates image-based data transmission, consumption of web APIs, GUI-driven interaction, and feature-based image analysis within a Java application.
