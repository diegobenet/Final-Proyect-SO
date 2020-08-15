import java.util.*;
import java.io.*;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class FXMLController implements Initializable {

    @FXML private Label nombr;
    @FXML private Label nombreEjec;
    @FXML private Label tiempoLlegada;
    @FXML private Label tiempoAct;
    @FXML private Label cpuAsignado;
    @FXML private Label envejecimiento;
    @FXML private Label cpuRestante;
    @FXML private Label qRestante;
    
    @FXML private TextField quantum = new TextField();;
    @FXML private TextField numPag;
    @FXML private TextField ejecTot;
    @FXML private ComboBox cBInterrupcion;
    @FXML private ComboBox cBPaginas;
    @FXML private ComboBox cBCpu;
    @FXML private ComboBox cBReemplazo;
    @FXML private VBox cajaListos;
    @FXML private VBox cajaBloqueados;
    @FXML private VBox cajaTerminados;
    @FXML private VBox cajaEjecutado;
    @FXML private VBox vBCpu;
    @FXML private VBox vBPaginas;
    @FXML private HBox hBTiempoLlegada;
    @FXML private HBox hBQuantum;
    @FXML private HBox hBcpuRestante;
    @FXML private HBox hBEnvejecimiento;
    @FXML private HBox hBCajaCpu;
    @FXML private ScrollPane scrollPaneListos;
    @FXML private ScrollPane scrollPaneBloqueados;
    @FXML private ScrollPane scrollPaneTerminados;
    @FXML private ScrollPane scrollPanePaginas;
    @FXML private String nombreprocesoactual;
    @FXML private String paginaAnterior;
        
    SO so = new SO();
    List<Proceso> procesosListos = new ArrayList<>();
    List<Proceso> procesosBloqueados = new ArrayList<>();
    List<Proceso> procesosTerminados = new ArrayList<>();
    
    @FXML
    private void botonGenerarProceso(ActionEvent event) {
    int ejecTotal = Integer.parseInt(ejecTot.getText());
    int numeroP = Integer.parseInt(numPag.getText());
    int maxP = so.getMaxPags();
    int estado = 0;
    
       if((ejecTot.getText().isEmpty() || Integer.parseInt(ejecTot.getText())==0) || numeroP > maxP){
         //System.out.println("error, Ejecucion Total tiene que ser mayor a 1");
       }else{
          Proceso p = new Proceso(so.getReloj(), ejecTotal, estado, numeroP);
          so.insertarProceso(p);
          nombr.setText(""+(so.getNumProcesos()+1)); 
           
           
       if(!(quantum.getText().isEmpty()) && Integer.parseInt(quantum.getText()) > 0){ //-----
          int q = Integer.parseInt(""+quantum.getText());
          so.cpu.setQuantum(q);
       }else{
         so.cpu.setQuantum(5);
       }
       consultarProcesos();
       tiempoAct.setText(""+so.getReloj());
      }
    }

    @FXML
    private void botonIniciarSO(ActionEvent event) {
      int p=so.getMaxPags();
      int i=-1;
      String pg= cBPaginas.getValue().toString();
      while(p >= 0){
         if(pg.equals("Pagina "+Integer.toString(p))){
            i=p;
         }
         p--;
      }
      if(i == -1){
         System.out.println("ninguna pagina seleccionada");
         System.out.println("i ="+i);
         System.out.println("pg = "+pg);
      }else{
        so.iniciar(i);
        tiempoAct.setText(""+so.getReloj());
        nombr.setText(""+(so.getNumProcesos()+1));
        if(!(quantum.getText().isEmpty()) && Integer.parseInt(quantum.getText()) > 0){
          int q = Integer.parseInt(""+quantum.getText());
          so.cpu.setQuantum(q);
       }else{
         so.cpu.setQuantum(5);
       }
        consultarProcesos();
       }
    }
    
   @FXML
    private void botonInterrupcion(ActionEvent event) {
       int i=-1;
       String in= cBInterrupcion.getValue().toString();
       if(in == "SVC de solicitud de I/O"){
         i = 0;
       }else if(in == "SVC de terminación normal"){
         i = 1;
       }else if(in == "SVC de solitud de fecha"){
         i = 2;
       }else if(in == "Error de programa"){
         i = 3;
       }else if(in == "Externa de quantum expirado"){
         i = 4;
       }else if(in == "Dispositivo de I/O"){
         i = 5;
       }
       so.inter.interrumpir(i);
       tiempoAct.setText(""+so.getReloj());
       if(!(quantum.getText().isEmpty()) && Integer.parseInt(quantum.getText()) > 0){
          int q = Integer.parseInt(""+quantum.getText());
          so.cpu.setQuantum(q);
       }else{
         so.cpu.setQuantum(5);
       }
       consultarProcesos();
   }
   @FXML
    private void botonResetNur(ActionEvent event) {
       so.cpu.resetNur();
       consultarProcesoEjec();
       tiempoAct.setText(""+so.getReloj());
    }
   @FXML
    private void botonPlanificacion(ActionEvent event) {
       hBTiempoLlegada.setStyle(null);
       hBQuantum.setStyle(null);
       hBcpuRestante.setStyle(null);
       hBEnvejecimiento.setStyle(null);
       hBCajaCpu.getChildren().clear();
       hBCajaCpu.setMinWidth(0);
       hBCajaCpu.setMinHeight(0);
       
       byte i=-1;
       String in= cBCpu.getValue().toString();
       if(in == "FIFO"){
         i = 0;
         qRestante.setText("n/a");
         hBTiempoLlegada.setStyle("-fx-background-color: #c4a8ff ;");
       }else if(in == "Round Robin"){
         i = 1;
         CajaQuantum();
         hBQuantum.setStyle("-fx-background-color: #c4a8ff ;");
       }else if(in == "SRT"){
         i = 2;
         qRestante.setText("n/a");
         hBcpuRestante.setStyle("-fx-background-color: #c4a8ff ;");
       }else if(in == "HRRN"){
         qRestante.setText("n/a");
         hBEnvejecimiento.setStyle("-fx-background-color: #c4a8ff ;");
         i = 3;
       }
       so.cpu.setPlanificacion(i);
       tiempoAct.setText(""+so.getReloj());
       if(!(quantum.getText().isEmpty()) && Integer.parseInt(quantum.getText()) > 0){
          int q = Integer.parseInt(""+quantum.getText());
          so.cpu.setQuantum(q);
       }else{
         so.cpu.setQuantum(5);
       }
       consultarProcesos();
   }
   
   @FXML
   private void botonReemplazo(ActionEvent event) {
       byte i=-1;
       String in= cBReemplazo.getValue().toString();
       if(in == "FIFO"){
         i = 0;
       }else if(in == "LRU"){
         i = 1;
       }else if(in == "LFU"){
         i = 2;
       }else if(in == "NUR"){
         i = 3;
       }
       so.cpu.setReemplazo(i);
   }
   
   private void CajaQuantum(){
      Label labQ = new Label();
      Region r = new Region();
      r.setMaxWidth(300);
      r.setMaxHeight(300);
      r.setMinWidth(20);
      r.setMinHeight(20);
      hBCajaCpu.setMinWidth(80);
      hBCajaCpu.setMinHeight(38);
      labQ.setText("Tamaño Quantum");
      labQ.setFont(Font.font("System", 12));
      hBCajaCpu.getChildren().add(labQ);
      hBCajaCpu.getChildren().add(r);
      quantum.setStyle("-fx-pref-width: 35;");
      hBCajaCpu.getChildren().add(quantum);
      
      quantum.textProperty().addListener((observable, oldValue, newValue) -> {
         int num = 0;
         try { num = Integer.parseInt(newValue); } catch (Exception e) {}
         if(newValue != "" && num >= 1){
            try {
            so.cpu.setQuantum(Integer.parseInt(newValue));
            so.cpu.pEjec.setQRestante(Integer.parseInt(newValue));
            qRestante.setText(Integer.toString(so.cpu.pEjec.getQRestante()));
            } catch (Exception e) {}
         }
         else {
            so.cpu.setQuantum(5);
            so.cpu.pEjec.setQRestante(5);
            qRestante.setText("5");
         }
      });
   }
    @FXML
    private void botonQ(ActionEvent event) {
       int q = Integer.parseInt(quantum.getText());
       so.cpu.setQuantum(q);
       System.out.println(q);
    }
   
   private void consultarProcesoEjec(){
      cajaEjecutado.getChildren().clear();
      if(so.cpu.pEjec != null){
         crearCajaProcesos("proceso Ejecutado", 0);
         nombreEjec.setText(Integer.toString(so.cpu.pEjec.getNombre()));
         cpuAsignado.setText(Integer.toString(so.cpu.pEjec.getCpuAsignado()));
         tiempoLlegada.setText(Integer.toString(so.cpu.pEjec.getLlegada()));
         envejecimiento.setText(Integer.toString(so.cpu.pEjec.getEnvejecimiento()));
         cpuRestante.setText(Integer.toString(so.cpu.pEjec.getCpuRestante()));
         if(so.cpu.getPlanificacion() != 1){
            qRestante.setText("n/a");
         }else{
            qRestante.setText(Integer.toString(so.cpu.pEjec.getQRestante()));
         }
      }else{
         nombreEjec.setText("n/a");
         cpuAsignado.setText("n/a");
         tiempoLlegada.setText("n/a");
         envejecimiento.setText("n/a");
         cpuRestante.setText("n/a");
         qRestante.setText("n/a");
      }
      try{
      cBPaginas.getItems().clear();
      vBPaginas.getChildren().clear();
      if (so.cpu.pEjec != null && so.cpu.pEjec.getPaginas() != null) {
         for(int i = 0; i < so.cpu.pEjec.getPaginas().length; i++){
            consultarPaginas(i);
            consultarPag(i);
         }
      }
      if(!(nombreprocesoactual.equals(""+so.cpu.pEjec.getNombre()))){
         cBPaginas.setValue("Pagina 0");
      }else{
         cBPaginas.setValue(paginaAnterior);
      }  
      nombreprocesoactual = Integer.toString(so.cpu.pEjec.getNombre());
      } catch (Exception e) {}
   }
   private void consultarPaginas(int i){
   Region r1 = new Region();
   Region r2 = new Region();
   Region r3 = new Region();
   Region r4 = new Region();
   Region r5 = new Region();
   
   r1.setPrefWidth(12);
   r2.setPrefWidth(12);
   r3.setPrefWidth(12);
   r4.setPrefWidth(12);
   r5.setPrefWidth(12);
   
   Label mPag = new Label();
   mPag.setPrefWidth(36);
   mPag.setAlignment(Pos.CENTER);
   Label mR = new Label();
   mR.setPrefWidth(8);
   mR.setAlignment(Pos.CENTER);
   Label mLlegada = new Label();
   mLlegada.setPrefWidth(39);
   mLlegada.setAlignment(Pos.CENTER);
   Label mUlt = new Label();
   mUlt.setPrefWidth(53);
   mUlt.setAlignment(Pos.CENTER);
   Label mAccesos = new Label();
   mAccesos.setPrefWidth(41);
   mAccesos.setAlignment(Pos.CENTER);
   Label mNur = new Label();
   mNur.setPrefWidth(25);
   mNur.setAlignment(Pos.CENTER);
   HBox hP = new HBox();   
   hP.setAlignment(Pos.CENTER);
   hP.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black;");
   String nur="";
   nur = ""+so.cpu.pEjec.paginas[i].getNur1()+so.cpu.pEjec.paginas[i].getNur2();
   
   mPag.setText(Integer.toString(so.cpu.pEjec.paginas[i].getNumero()));
   mR.setText(Integer.toString(so.cpu.pEjec.paginas[i].getResidencia()));
   mLlegada.setText(Integer.toString(so.cpu.pEjec.paginas[i].getLlegada()));
   mUlt.setText(Integer.toString(so.cpu.pEjec.paginas[i].getUltAcceso()));
   mAccesos.setText(Integer.toString(so.cpu.pEjec.paginas[i].getNumAccesos()));
   mNur.setText(nur);
   hP.getChildren().addAll(mPag,r1,mR,r2,mLlegada,r3,mUlt,r4,mAccesos,r5,mNur);
   hP.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black; -fx-background-color: orange;");
   vBPaginas.getChildren().add(hP);
      
   }
   
   private void crearCajaProcesos(String p, int i){
      so.cpu.ordenarListos();
      
      Label lis = new Label();
      HBox h = new HBox();
      h.setAlignment(Pos.CENTER);
      //h.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null , null)));
      lis.setAlignment(Pos.CENTER_LEFT);
      try {
      if(p=="procesos Listos"){
         lis.setText(""+procesosListos.get(i).getNombre());
         h.getChildren().add(lis);
         h.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black;");
         cajaListos.getChildren().add(h);
         }
      if(p=="procesos Bloqueados"){
         lis.setText(""+procesosBloqueados.get(i).getNombre());
         h.getChildren().add(lis);
         h.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black;");
         cajaBloqueados.getChildren().add(h);
      }
      if(p=="procesos Terminados"){
         lis.setText(""+procesosTerminados.get(i).getNombre());
         h.getChildren().add(lis);
         h.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black;");
         cajaTerminados.getChildren().add(h);
      }
      if(p=="proceso Ejecutado"){
         lis.setText(""+so.cpu.pEjec.getNombre());
         h.getChildren().add(lis);
         h.setStyle("-fx-border-width: 0 0 1 0; -fx-border-color:black; -fx-background-color: orange;");
         cajaEjecutado.getChildren().add(h);
      }
      } catch (Exception e) {}
    }
    public void consultarProcesos(){
      cajaListos.getChildren().clear();
      cajaBloqueados.getChildren().clear();
      cajaTerminados.getChildren().clear();
      cajaEjecutado.setStyle(null);
      procesosListos = so.cpu.getProcesosListos();
      procesosBloqueados = so.cpu.getProcesosBloqueados();
      procesosTerminados = so.cpu.getProcesosTerminados();
       for (int i = 0; i < procesosListos.size(); i++) {
         crearCajaProcesos("procesos Listos", i);
       }
       for (int i = 0; i < procesosBloqueados.size(); i++) {
         crearCajaProcesos("procesos Bloqueados", i); 
       }
       for (int i = 0; i < procesosTerminados.size(); i++) {
         crearCajaProcesos("procesos Terminados", i); 
       }
        consultarProcesoEjec();
    }
    public void consultarPag(int i){
         cBPaginas.getItems().addAll("Pagina "+(i));
    }
    
    @FXML
    private void botonPaginas(ActionEvent event) {
       try{
         paginaAnterior = cBPaginas.getValue().toString();
       } catch (Exception e) {}
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
      scrollPaneListos.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneListos.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneListos.setFitToWidth(true);
      
      scrollPaneBloqueados.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneBloqueados.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneBloqueados.setFitToWidth(true);
      
      scrollPaneTerminados.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneTerminados.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPaneTerminados.setFitToWidth(true);
      
      scrollPanePaginas.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPanePaginas.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      scrollPanePaginas.setFitToWidth(true);
      
      cBInterrupcion.getItems().addAll("SVC de solicitud de I/O","SVC de terminación normal", "SVC de solitud de fecha", "Error de programa", "Externa de quantum expirado", "Dispositivo de I/O");
      cBInterrupcion.setValue("SVC de solicitud de I/O");
      
      cBCpu.getItems().addAll("FIFO","Round Robin","SRT","HRRN");
      cBCpu.setValue("FIFO");
      
      cBReemplazo.getItems().addAll("FIFO","LRU","LFU","NUR");
      cBReemplazo.setValue("FIFO");

      nombreprocesoactual = Integer.toString(so.cpu.pEjec.getNombre());
      quantum.setText("5");
      so.cpu.setPlanificacion((byte)0);
      hBTiempoLlegada.setStyle("-fx-background-color: #c4a8ff;");
      nombr.setText(""+(so.getNumProcesos()+1));
      consultarProcesos();
      cBPaginas.setValue("Pagina 0");
      paginaAnterior = cBPaginas.getValue().toString();
      tiempoAct.setText(""+so.getReloj());
      hBCajaCpu.setMinWidth(0);
      hBCajaCpu.setMinHeight(0);
    }
}