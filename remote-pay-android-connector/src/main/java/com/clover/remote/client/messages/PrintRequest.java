package com.clover.remote.client.messages;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object for requesting a print job.
 */
public class PrintRequest extends BaseRequest {

  private List<Bitmap> images = new ArrayList<>();
  private List<String> imageURLs = new ArrayList<>();
  private List<String> text = new ArrayList<>();
  private String printRequestId = null;
  private String printDeviceId = null;

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param image Image to print
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(Bitmap image, String printRequestId, String printDeviceId){
    this.images.add(image);
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print an image at a given URL
   * @param imageUrl URL to the image to print
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(String imageUrl, String printRequestId, String printDeviceId){
    this.imageURLs.add(imageUrl);
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print an array of strings to print
   * @param text Array of strings to be printed
   * @param printRequestId identifier to give to the print job, so it can be later queried
   * @param printDeviceId identifier to specify printer to use
   */
  public PrintRequest(List<String> text, String printRequestId, String printDeviceId){
    for (String line: text) {
      this.text.add(line);
    }
    this.printRequestId = printRequestId;
    this.printDeviceId = printDeviceId;
  }
  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param image Image to print
   */
  public PrintRequest(Bitmap image){
    this(image, null, null);
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param imageUrl URL to the image to print
   */
  public PrintRequest(String imageUrl){
    this(imageUrl, null, null);
  }

  /**
   * Constructor
   *
   * Create a PrintRequest to print a given image
   * @param text Array of strings to be printed
   */
  public PrintRequest(List<String> text){
    this(text, null, null);
  }

  /**
   * Get the field value
   *
   * @return Image to print
   */
  public List<Bitmap> getImages(){
    return this.images;
  }

  /**
   * Get the field value
   *
   * @return URL of image to print
   */
  public List<String> getImageURLs() {
    return this.imageURLs;
  }

  /**
   * Get the field value
   *
   * @return list of strings to be printed
   */
  public List<String> getText() {
    return this.text;
  }

  /**
   * Get the field value
   *
   * @return identifier to give to the print job, so it can later be queried
   */
  public String getPrintRequestId() {
    return printRequestId;
  }

  /**
   * Get the field value
   *
   * @return identifier to specify printer to use
   */
  public String getPrintDeviceId() {
    return printDeviceId;
  }


  /**
   * Set the field value
   *
   * @param printRequestId id of print job
   */
  public void setPrintRequestId(String printRequestId) {
    this.printRequestId = printRequestId;
  }

  /**
   * Set the field value
   *
   * @param printDeviceId id of the printer
   */
  public void setPrintDeviceId(String printDeviceId) {
    this.printDeviceId = printDeviceId;
  }
}
