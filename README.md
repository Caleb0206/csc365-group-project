# csc365-group-project

## Database login

hostname: ambari-node5.csc.calpoly.edu
username: songsdatabase
default schema: ambari-node5.csc.calpoly.edu 
password: coding 

## Steps To Get Project Running

1. Download sdk for javafx from here: https://gluonhq.com/products/javafx/
2. Extract sdk folder into a known location (I would suggest just home directory, C:\javafx-sdk-23.0.2 for windows)
3. Go to java project in IdeaProjects, go to file/project+structure, under project settings, click libraries, click + button to add new project library,
then click Java, and finally put in the path to the lib file (such as C:\javafx-sdk-23.0.2\lib), and click ok
4. Then go to run/edit configurations, click + for add new configurations, and then click application.
Name it JavaFX App, click modify options (in blue text), and click Add VM options.
Under VM Options, paste this: --module-path C:\javafx-sdk-23.0.2\lib --add-modules=javafx.controls,javafx.fxml with your actual path to the lib folder.
Put this as your main class: com.example.MainApp
