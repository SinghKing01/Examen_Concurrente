package uib.examenp1;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProblemaBabuins {
    static final int NUM_BABUINS = 10;
    
    public static void main(String[] args) throws InterruptedException {
        Corda monitor = new Corda();
        Thread[] babuinsNord = new Thread[NUM_BABUINS/2];
        Thread[] babuinsSud = new Thread[NUM_BABUINS/2];
        //Creamos y lanzamos los procesos del norte
        for (int i = 0; i < NUM_BABUINS/2; i++) {
            babuinsNord[i] = new Thread(new BabuinNord(monitor));
            babuinsNord[i].start();
        }
        //Creamos y lanzamos los procesos del sud
        for (int i = 0; i < NUM_BABUINS/2; i++) {
            babuinsSud[i] = new Thread(new BabuinSud(monitor));
            babuinsSud[i].start();
        }
        //esperamos a los procesos
        for (int i = 0; i < NUM_BABUINS/2; i++) {
            babuinsNord[i].join();
        }
        for (int i = 0; i < NUM_BABUINS/2; i++) {
            babuinsSud[i].join();
        }
        System.out.println("ACABA LA SIMULACIÓ");
    }
}

class Corda {
    private volatile int babuinsDins = 0;
    private final Lock lock = new ReentrantLock(); // para exclusión mutua
    private final Condition entrar; //Variable condición Los procesos solamente se bloquean al entrar
    private volatile int direccioDins;

    public Corda() {
        this.entrar = lock.newCondition();
    }
    
    void entrar(int direccio){ //con ué direccion quiere entrar: 0 = sud, 1 = nord
        lock.lock();
        try{
            //si en la cuerda están los babuinos con la misma dirección pero ya hay 3, se bloquean
            //si en la cuerda está la dirección diferente que la mia y la dirección no es -1, se bloquean
            while ((direccioDins == direccio && babuinsDins == 3) || (direccioDins != direccio && direccioDins != -1)) {
                entrar.await();
            }
            direccioDins = direccio; //cambiamos la direccion en la cuera
            babuinsDins++; // icrementamos el número de babuinos
        } catch (InterruptedException ex) {
        }finally{
            lock.unlock();
        }
    }
    
    void sortir(){
        lock.lock();
        try{
            babuinsDins--; //al salir, decrementamos el número de babuinos
            if(babuinsDins == 0){
                direccioDins = -1; //si ya no hay más, ponemos la dirección -1
            }
            if(babuinsDins < 3){ //si hay menos babuinos de los máximos        
                entrar.signal(); //liberamos la cola de los procesos
            }
        }finally{
            lock.unlock();
        }
    }
}

class BabuinNord implements Runnable {
    static volatile int id = 0; //para asignar ids a los procesos, lo iremos incrementando
    int myID;
    Corda monitor;
    
    public BabuinNord(Corda monitor) {
        myID = id;
        id++;
        this.monitor = monitor;
    }
    
    @Override
    public void run() {
        System.out.printf("BON DIA som el babuí Nord %d i vaig cap al nord\n",myID);
        for (int i = 0; i < 3; i++) {
            try {
                monitor.entrar(1); //1 quiere decir que va a la dirección norte
                System.out.printf("Nord %d: És a la corda i travessa cap al nord\n",myID);
                Thread.sleep(1000);//tiempo para cruzar
                System.out.printf("Nord %d Ha arribat a la vorera\n",myID);
                System.out.printf("Nord %d fa la volta %d de %d\n",myID,i+1,3);
                monitor.sortir();
                Thread.sleep(1000);//timepo para subir
            } catch (InterruptedException ex) {
            }
        }
    }
    
}

class BabuinSud implements Runnable {
    static volatile int id = 0;
    int myID;
    Corda monitor;
    
    public BabuinSud(Corda monitor) {
        this.myID = id;
        id++;
        this.monitor = monitor;
    }
    
    @Override
    public void run() {
        System.out.printf("\tBON DIA som el babuí Sud %d i vaig cap al sud\n",myID);
        for (int i = 0; i < 3; i++) {
            try {
                monitor.entrar(0); //0 quiere decir que va a la dirección sud
                System.out.printf("\tSud %d: És a la corda i travessa cap al sud\n",myID);
                Thread.sleep(1000);//temps per creuar
                System.out.printf("\tSud %d Ha arribat a la vorera\n",myID);
                System.out.printf("\tSud %d fa la volta %d de %d\n",myID,i+1,3);
                monitor.sortir();
                Thread.sleep(1000);//temps per pujar
            } catch (InterruptedException ex) {
            }
        }
    }
    
}
