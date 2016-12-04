package raspiframe;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.util.List;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import javafx.scene.layout.AnchorPane;
import javafx.animation.SequentialTransition;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.util.Duration;
import java.util.Collections;
import javafx.collections.ListChangeListener;
import raspiframe.utilities.myImageView;
import raspiframe.utilities.Setup;

/**
 *
 * @author David Hinchliffe <belgoi@gmail.com>
 */
public class PhotoFrameController implements Initializable
{
    @FXML
    private AnchorPane documentRoot;
    @FXML
    private AnchorPane photoFrameRoot;
    @FXML
    private AnchorPane slideShow;
    private PhotoFrameModel model;
    private  List<myImageView> pictures;
    public PhotoFrameController()
    {       
        pictures=new ArrayList();
    }
    public PhotoFrameController(PhotoFrameModel model)
    {
        this();
        this.model=model;
    }
  
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        model.getObservablePhotoList().addListener(new ListChangeListener()
        {
            @Override
            public void onChanged(ListChangeListener.Change change)
            {
                //have to iterate through the changes
                while(change.next())
                    //only interested in the pictures that have been added or deleted
                    if((change.wasAdded()) || change.wasRemoved())
                        //copy the pictures over to the array that will be used in the slideshow
                        pictures=model.getObservablePhotoList();  
            }
        });
        
        //Load the photos for the slideshow
        model.loadImgFiles();
        startSlideShow();
    }

    public void startSlideShow()
    {
        //Sequential Transition to add the fade in, pause, and fade out transitions for each slide
        SequentialTransition slides;
        //Sequential Transition to play each slide
        SequentialTransition slideshow= new SequentialTransition();
        //shuffle the pictures so the pictures won't always be displayed in the same order
        Collections.shuffle(pictures);
        for (ImageView picture:pictures)
        {
            slides=new SequentialTransition();
            //set fade in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(Setup.fadeInDuration()),picture);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
            //set how long to display picture
            PauseTransition showPicture = new PauseTransition(Duration.seconds(Setup.PauseDuration()));
            //set fade out transition
            FadeTransition fadeOut=new FadeTransition(Duration.millis(Setup.fadeOutDuration()),picture);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
            //add transitions to the slide
            slides.getChildren().addAll(fadeIn,showPicture,fadeOut);
            //since all pictures are stacked on the screen at once, 
            //the opacity has to be set to 0
            picture.setOpacity(0);
            //add the pictures to the photo frame
            photoFrameRoot.getChildren().add(picture);
            //add the slides to the slideshow
            slideshow.getChildren().add(slides);
        }
        //go through the slideshow once before it is restarted and refreshed with any changes
        slideshow.setCycleCount(1);
        slideshow.setOnFinished(new EventHandler<ActionEvent>()
       {
           public void handle(ActionEvent event)
           {
               //clear the child nodes from the photoFrame root
               photoFrameRoot.getChildren().clear();
               //Restart the slideshow to randomize the order of the pictures
               startSlideShow();    
           }
       });
        slideshow.play();
        
    }
    
}
