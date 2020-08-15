import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.*;
import java.io.*;

public class JavaFXApplication1 extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("prueba2SO.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

class SO {
   int reloj;
   int maxPags;
   int numProcesos;
   CPU cpu;
   Interruptor inter;
   // CONFIGURACION **********************************
   final String DIVISOR = ",";
   final boolean DESBLOQUEAR_EN_5 = true;
   final boolean SACAR_PAGS_TERMINADAS = true;
   final boolean RESET_TODOS_NUR = true;
   final boolean RESET_PAGINA_REENTRANTE = true;
   // CONFIGURACION **********************************
   
   public SO () {
      File f;
      FileInputStream f2;
      InputStreamReader f3;
      BufferedReader f4;
      String linea;
      String[] separado;
      int numProcs;
      
      this.cpu = new CPU(this); 
      this.inter = new Interruptor(this.cpu);
      this.numProcesos = 0;
      
      try {
         f = new File("entrada2.txt");
         f2 = new FileInputStream(f);
         f3 = new InputStreamReader(f2);
         f4 = new BufferedReader(f3);
         
         linea = f4.readLine();
         separado = linea.split(DIVISOR);
         this.reloj = Integer.parseInt(separado[1]);
         this.maxPags = Integer.parseInt(separado[0]);
         
         linea = f4.readLine();
         separado = linea.split(DIVISOR);
         numProcs = Integer.parseInt(separado[0]);
         
         String[] datosProc;
         int numPagsProc;
         int est;
         
         Proceso[] procesos = new Proceso[numProcs];
         
         for (int i = 0; i < numProcs; i++) {
            datosProc = f4.readLine().split(DIVISOR);
            numPagsProc = Integer.parseInt(f4.readLine().split(", ")[0]);
            
            est = Integer.parseInt(datosProc[2]); 
            if (est == 3) {
               est = 0;
            }
            
            Proceso p = new Proceso(Integer.parseInt(datosProc[0]), Integer.parseInt(datosProc[1]), est);
            procesos[i] = p;
            this.numProcesos++;
            p.setEnvejecimiento(this.reloj - Integer.parseInt(datosProc[0]));
            p.setNombre(numProcesos);
            p.setQRestante(5);
            
            if (p.estado == 1) {
               cpu.setPEjec(p);
            } else if (p.estado == 2) {
               cpu.getProcesosBloqueados().add(p);
            } else if (p.estado == 0) {
               cpu.getProcesosListos().add(p);
            }
            
            String[] datosPag;
            int[] datosPagInt = new int[8];
            Pagina[] paginas = new Pagina[numPagsProc];
            for (int j = 0; j < numPagsProc; j++) {
               datosPag = f4.readLine().split(DIVISOR);
               datosPagInt[0] = p.getNombre();
               datosPagInt[1] = j;
               for (int k = 0; k < datosPag.length; k++) {
                  datosPagInt[k + 2] = Integer.parseInt(datosPag[k]);
               }
               paginas[j] = new Pagina(datosPagInt[0], datosPagInt[1], datosPagInt[2], datosPagInt[3], datosPagInt[4], datosPagInt[5], datosPagInt[6], datosPagInt[7]);
               if (paginas[j].getResidencia() == 1) {
                  cpu.memPpal.add(paginas[j]);
               }
            }
            p.setPaginas(paginas);
         }
         
         Arrays.sort(procesos, Comparator.comparing(Proceso::getLlegada));
         for (int i = 0; i < procesos.length; i++) {
            procesos[i].setNombre(i + 1);
         }
         
         f4.close();
      }
      catch(IOException e) {
         System.out.println("Error en archivo.");
      }
   }
   
   public int getReloj() {
      return reloj;
   }
   public int getMaxPags () {
      return maxPags;
   }
   public int getNumProcesos() {
      return numProcesos;
   }
   
   public void insertarProceso(Proceso p) {
      if (this.cpu.getPEjec() == null) {
         this.cpu.setPEjec(p);
      }
      else {
         this.cpu.getProcesosListos().add(p);
      }
      numProcesos++;
      p.setNombre(this.numProcesos);
      this.aumentarReloj();
   }
   
   public void iniciar(int pag) {
      if (this.cpu.getPEjec() != null) {
         aumentarReloj();
         cpu.ejecutarInstruccion(pag);
      }
   }
   
   public void aumentarReloj() {
      this.reloj++;
   }
}

class Interruptor {
   CPU cpuInt;
   
   public Interruptor (CPU cpuInt) {
      this.cpuInt = cpuInt;
   }
   
   public void interrumpir(int inter) {
      cpuInt.so.aumentarReloj();
      
      if (cpuInt.pEjec == null && (inter != 5)) {
         cpuInt.so.reloj--;
         return;
      }
      
      if (inter == 0) { // SVC de solicitud de I/O
         cpuInt.intercambio(2);
      }
      else if (inter == 1) { //SVC de terminación normal
         cpuInt.intercambio(3);
      }
      else if (inter == 2) { //SVC de solitud de fecha
         cpuInt.intercambio(0);   
      }
      else if (inter == 3) { //error de programa
         cpuInt.intercambio(3);
      }
      else if (inter == 4) { //externa de quantum expirado
         cpuInt.intercambio(0);
      }
      else if (inter == 5) { //dispositivo de I/O
         if (cpuInt.procesosBloqueados.size() == 0) {
         cpuInt.so.reloj--;
         return;
         }
         Proceso p = cpuInt.getProcesosBloqueados().get(0);
         cpuInt.getProcesosBloqueados().remove(0);
         p.setEstado(0);
         p.setCuentaParaDesbloquearse(0);
         cpuInt.getProcesosListos().add(p);
         cpuInt.intercambio(0);
         if (cpuInt.planificacion == 1 && cpuInt.procesosListos.size() >= 2) { //Agregado para order correcto
            Proceso proc = cpuInt.procesosListos.get(cpuInt.procesosListos.size() - 2);
            cpuInt.procesosListos.remove(proc);
            cpuInt.procesosListos.add(proc);
         }
      }
   }
}

class CPU {
   byte planificacion;
   byte reemplazo;
   int quantum;
   Proceso pEjec;
   List<Proceso> procesosListos = new ArrayList<>();
   List<Proceso> procesosBloqueados = new ArrayList<>();
   List<Proceso> procesosTerminados = new ArrayList<>();
   Interruptor inter;
   SO so;
   // TAMAÑO DE LA MEMORIA ************************
   int memoriaTotal = 4;
   // TAMAÑO DE LA MEMORIA ************************
   List<Pagina> memPpal = new ArrayList<>();
   
   public CPU (SO so) {
      this.inter = new Interruptor(this);
      this.so = so;
   }
   
   public void setPEjec (Proceso pEjec) {
      this.pEjec = pEjec;
   }
   public void setPlanificacion(byte planificacion) {
      this.planificacion = planificacion;
   }
   public void setReemplazo(byte reemplazo) {
      this.reemplazo = reemplazo;
   }
   public void setQuantum(int quantum) {
      this.quantum = quantum;
   }
   
   public Proceso getPEjec () {
      return pEjec;
   }
   public int getQuantum() {
      return quantum;
   }
   public List<Proceso> getProcesosListos () {
      return procesosListos;
   }
   public List<Proceso> getProcesosBloqueados () {
      return procesosBloqueados;
   }
   public List<Proceso> getProcesosTerminados () {
      return procesosTerminados;
   }
   public byte getPlanificacion(){
      return planificacion;
   }
   
   public void intercambio(int estadoSalida) {
      if (pEjec != null) 
         pEjec.setEstado(estadoSalida);
      
      if (pEjec != null) {
         if (estadoSalida == 0) {
            procesosListos.add(pEjec);
         }
         if (estadoSalida == 2) {
            procesosBloqueados.add(pEjec);
         }
         if (estadoSalida == 3) {
            procesosTerminados.add(pEjec);
            if (so.SACAR_PAGS_TERMINADAS) {
               for (int i = 0; i < memPpal.size(); i++) {
                  if (memPpal.get(i).getProceso() == pEjec.getNombre()) {
                     memPpal.remove(i);
                  }
               }
            }
         }
      }
      
      ordenarListos();
      pEjec = null;
      
      try {
      if (procesosListos.size() > 0) {
         pEjec = procesosListos.get(0);
         pEjec.setEstado(1);
         procesosListos.remove(0);
      }
      } catch (Exception e) {}
       
      
      if (planificacion == 1 && pEjec != null) {
         pEjec.setQRestante(getQuantum());
      }
      
      if (pEjec != null) pEjec.setEnvejecimiento(so.reloj - pEjec.llegada - pEjec.cpuAsignado);
   }
   
   public void ejecutarInstruccion(int pag) {
      if (pEjec == null) {
         return;
      }
      
      Pagina pagActual = pEjec.getPaginas()[pag];
      
      System.out.println("Pagina " + pag + ", residencia " + pagActual.getResidencia() + ", tamanio memoria " + memoriaTotal + ", usada ahorita " + memPpal.size());
      if (pagActual.getResidencia() == 0 && memPpal.size() == memoriaTotal) {
         if (reemplazo == 0) { //FIFO
            memPpal.sort(Comparator.comparing(Pagina -> Pagina.llegada));
            reemplazar(memPpal.get(0));
         }
         else if (reemplazo == 1) { //LRU
            memPpal.sort(Comparator.comparing(Pagina -> Pagina.ultAcceso));
            reemplazar(memPpal.get(0));
         }
         else if (reemplazo == 2) { //LFU
            memPpal.sort(Comparator.comparing(Pagina -> Pagina.numAccesos));
            reemplazar(memPpal.get(0));
         }
         else if (reemplazo == 3) { //NUR
            memPpal.sort(Comparator.comparing(Pagina -> Pagina.llegada));
            String nurs = "00";
            int candidato = -1;
            for (int i = 0; i < 4; i++) {
               if (i == 1) nurs = "10";
               if (i == 2) nurs = "01";
               if (i == 3) nurs = "11";
               for (int j = 0; j < memPpal.size(); j++) {
                  if ('0' + memPpal.get(j).getNur1() == nurs.charAt(0) && '0' + memPpal.get(j).getNur2() == nurs.charAt(1)) {
                     candidato = j;
                     break;
                  }
               }
               if (candidato != -1) break;
            }
            reemplazar(memPpal.get(candidato));
         }
      }
      
      if (pagActual.getResidencia() == 0) {
         pagActual.setLlegada(so.reloj - 1);
         pagActual.setResidencia(1);
         memPpal.add(pagActual);
         if (so.RESET_PAGINA_REENTRANTE) {
            pagActual.setNumAccesos(0);
            pagActual.setContadorModif(0);
            pagActual.setNur1(0);
            pagActual.setNur2(0);
         }
      }
      
      pagActual.setUltAcceso(so.reloj - 1);
      pagActual.setNumAccesos(pagActual.getNumAccesos() + 1);
      pagActual.setNur1(1);
      pagActual.setContadorModif(pagActual.getContadorModif() + 1);
      if (pagActual.getContadorModif() == 5) {
         pagActual.setContadorModif(0);
         pagActual.setNur2(1);
      }
      
      pEjec.cpuAsignado++;
      pEjec.cpuRestante--;
      
      if (so.DESBLOQUEAR_EN_5) {
         try {
            for (int i = 0; i < procesosBloqueados.size(); i++) {
               if (procesosBloqueados.get(i) != null) {
                  procesosBloqueados.get(i).cuentaParaDesbloquearse++;
                  if (procesosBloqueados.get(i).cuentaParaDesbloquearse == 5) {
                     procesosBloqueados.get(i).cuentaParaDesbloquearse = 0;
                     inter.interrumpir(5);
                     pEjec.qRestante++;
                  }
                  break;
               }
            }
         } catch (Exception e) {}
      }
      
      if (this.planificacion == 1) {
         pEjec.qRestante--;
      }
      
      if (pEjec.cpuRestante == 0) {
         inter.interrumpir(1);
         return;
      }
      
      if (this.planificacion == 1) {
         if (pEjec != null && pEjec.qRestante == 0) {
            inter.interrumpir(4);
         }
      }
      
      if (pEjec == null) {
         for (int i = 0; i < procesosListos.size(); i++) {
            if (procesosListos.get(i) != null) {
               pEjec = procesosListos.get(i);
               procesosListos.remove(i);
               pEjec.setQRestante(getQuantum());
            }
         }
      }
   }
   
   public void reemplazar(Pagina sale) {
      sale.setResidencia(0);
      memPpal.remove(sale);
   }
   
   public void ordenarListos() {
      try {
      for (Proceso p : procesosListos) {
         p.setEnvejecimiento(so.reloj - p.llegada - p.cpuAsignado);
      }
      for (Proceso p : procesosBloqueados) {
         p.setEnvejecimiento(so.reloj - p.llegada - p.cpuAsignado);
      }
      } catch (Exception e) {}
   
      if (planificacion == 0) {
         procesosListos.sort(Comparator.comparing(Proceso -> Proceso.llegada));
      }
      else if (planificacion == 1) {
      
      }
      else if (planificacion == 2) {
         procesosListos.sort(Comparator.comparing(Proceso -> Proceso.cpuRestante));
      }
      else if (planificacion == 3) {
         for (Proceso p : procesosListos) {
            p.setValorR((p.envejecimiento + p.ejecTotal) / (p.ejecTotal * 1.0));
         }
         procesosListos.sort(Comparator.comparing((Proceso p) -> p.getValorR()).reversed());
      }
   }
   
   public void resetNur() {
      for (int i = 0; i < pEjec.getPaginas().length; i++) {
         pEjec.getPaginas()[i].setNur1(0);
         pEjec.getPaginas()[i].setNur2(0);
      }
      if (so.RESET_TODOS_NUR) {
         for (int i = 0; i < procesosListos.size(); i++) {
            Pagina[] pags = procesosListos.get(i).getPaginas();
            for (int j = 0; j < pags.length; j++) {
               pags[j].setNur1(0);
               pags[j].setNur2(0);
            }
         }
         for (int i = 0; i < procesosBloqueados.size(); i++) {
            Pagina[] pags = procesosBloqueados.get(i).getPaginas();
            for (int j = 0; j < pags.length; j++) {
               pags[j].setNur1(0);
               pags[j].setNur2(0);
            }
         }
      }
   }
}

class Proceso {
   int nombre;
   int estado;
   int llegada;
   int ejecTotal;
   int cpuAsignado;
   int envejecimiento;
   int numPaginas;
   int cpuRestante;
   int qRestante;
   double valorR;
   Pagina[] paginas;
   int cuentaParaDesbloquearse;
   
   public Proceso (int llegada, int ejecTotal, int estado) {
      setLlegada(llegada);
      setEjecTotal(ejecTotal);
      setEstado(estado);
      this.cpuRestante = ejecTotal;
      this.cuentaParaDesbloquearse = 0;
   }
   
   public Proceso(int llegada, int ejecTotal, int estado, int numPags) {
      this(llegada, ejecTotal, estado);
      Pagina[] paginas = new Pagina[numPags];
      for (int i = 0; i < numPags; i++) {
         paginas[i] = new Pagina(this.nombre, i);
      }
      setPaginas(paginas);
   }
   
   public void setLlegada (int llegada) {
      this.llegada = llegada;
   }
   public void setEjecTotal (int ejecTotal) {
      this.ejecTotal = ejecTotal;
   }
   public void setEstado (int estado) {
      this.estado = estado;
   }
   public void setQRestante (int qRestante) {
      this.qRestante = qRestante;
   }
   public void setEnvejecimiento (int envejecimiento) {
      this.envejecimiento = envejecimiento;
   }
   public void setNombre(int nombre) {
      this.nombre = nombre;
   }
   public void setValorR(double valorR) {
      this.valorR = valorR;
   }
   public void setPaginas(Pagina[] paginas) {
      this.paginas = paginas;
   }
   public void setCuentaParaDesbloquearse(int cuentaParaDesbloquearse) {
      this.cuentaParaDesbloquearse = cuentaParaDesbloquearse;
   }
   
   public int getCpuRestante() {
      return cpuRestante;
   }
   public int getEjecTotal() {
      return ejecTotal;
   }
   public int getCpuAsignado() {
      return cpuAsignado;
   }
   public int getEnvejecimiento() {
      return envejecimiento;
   }
   public int getNombre() {
      return nombre;
   }
   public int getLlegada() {
      return llegada;
   }
   public int getQRestante() {
      return qRestante;
   }
   public double getValorR() {
      return valorR;
   }
   public Pagina[] getPaginas() {
      return paginas;
   }
}

class Pagina {
   int proceso;
   int numero;
   int residencia;
   int llegada;
   int ultAcceso;
   int numAccesos;
   int contadorModif;
   int nur1;
   int nur2;
   
   public Pagina(int proceso, int numero, int residencia, int llegada, int ultAcceso, int numAccesos, int nur1, int nur2) {
      setProceso(proceso);
      setNumero(numero);
      setResidencia(residencia);
      setLlegada(llegada);
      setUltAcceso(ultAcceso);
      setNumAccesos(numAccesos);
      setNur1(nur1);
      setNur2(nur2);
      contadorModif = 0;
   }
   
   public Pagina(int proceso, int numero) {
      setProceso(proceso);
      setNumero(numero);
      setResidencia(0);
      setLlegada(0);
      setUltAcceso(0);
      setNumAccesos(0);
      setNur1(0);
      setNur2(0);
      contadorModif = 0;
   }
   
   public void setProceso(int proceso) {
      this.proceso = proceso;
   }
   public void setNumero(int numero) {
      this.numero = numero;
   }
   public void setResidencia(int residencia) {
      this.residencia = residencia;
   }
   public void setLlegada(int llegada) {
      this.llegada = llegada;
   }
   public void setUltAcceso(int ultAcceso) {
      this.ultAcceso = ultAcceso;
   }
   public void setNumAccesos(int numAccesos) {
      this.numAccesos = numAccesos;
   }
   public void setNur1(int nur1) {
      this.nur1 = nur1;
   }
   public void setNur2(int nur2) {
      this.nur2 = nur2;
   }
   public void setContadorModif(int contadorModif) {
      this.contadorModif = contadorModif;
   }
   
   public int getProceso() {
      return proceso;
   }
   public int getNumero(){
      return numero;
   }
   public int getResidencia(){
      return residencia;
   }
   public int getLlegada(){
      return llegada;
   }
   public int getUltAcceso(){
      return ultAcceso;
   }
   public int getNumAccesos(){
      return numAccesos;
   }
   public int getNur1(){
      return nur1;
   }
   public int getNur2(){
      return nur2;
   }
   public int getContadorModif() {
      return contadorModif;
   }
}