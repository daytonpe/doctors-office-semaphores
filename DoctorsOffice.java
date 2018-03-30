/*
Patrick Dayton
Project 2 -- Doctor's Office Simulator with Semaphores
CS 5348 -- Prof Ozbirn
Due: 31 March 2018
 */

package doctorsoffice;


// Java implementation of a producer and consumer
// that use semaphores to control synchronization.

import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class DoctorsOffice{
    int item;
    int currentID;
    int patientInOfficeID;
    List<Integer> waitingRoomList = new ArrayList<>();

    // semCon initialized with 0 permits
    // to ensure put() executes first
    static  Semaphore semCon = new Semaphore(0);
    static Semaphore semProd = new Semaphore(1);

    //start with one receptionist
    static Semaphore receptionistCon = new Semaphore(1);
    static Semaphore receptionistProd = new Semaphore(0);

    //waiting room queue
    static Semaphore waitingRoomSem = new Semaphore(0);

    static Semaphore doctorSem = new Semaphore(0); //cons
    static Semaphore nurseSem = new Semaphore(1); //prod


    public int getWaitingRoomListLength(){
        return waitingRoomList.size();
    }

    public int getWaitingRoomNext(){
        return waitingRoomList.get(0);
    }

    public boolean getReceptionist(int currentID){
        try{
            receptionistCon.acquire();
            Thread.sleep(500);
//            System.out.println("Patient "+currentID+" has checked in.");
            this.currentID = currentID;
        } catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
        receptionistProd.release();
        return true;
    }

    public void putReceptionist(){
        try{
            receptionistProd.acquire();
            Thread.sleep(500);
            System.out.println("Receptionist registers patient "+ currentID);
        } catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }

        receptionistCon.release(); //okay the receptionist is free
    }

    // Patient enters waiting room queue
    public void putWaitingRoom(int id){
        waitingRoomSem.release();
        waitingRoomList.add(id);
//        System.out.println("Waiting Room Occupancy: "+ waitingRoomSem.availablePermits());
//        System.out.println("Arraylist contains: " + waitingRoomList.toString());
    }

    public void nurseProduce(int NurseID){
//        System.out.println("Put Nurse Called");

        try{
            waitingRoomSem.acquire();
//            System.out.println("WAITINGROOM: "+waitingRoomList.size());
            try{
                nurseSem.acquire();
                int currentPatient = waitingRoomList.remove(0);
                System.out.println("Nurse "+NurseID+" takes patient "+ currentPatient + " to doctor's office." );
                patientInOfficeID = currentPatient;

            } catch(InterruptedException e) {
                System.out.println("InterruptedException caught");
            }

        } catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
        doctorSem.release();

    }

    public void doctorConsume(int DoctorID){
        try{
            doctorSem.acquire();
            System.out.println("Doctor " +DoctorID+ " listens to symptoms from patient "+ patientInOfficeID);
            Thread.sleep(500);
        } catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
        nurseSem.release();
    }
}
/******************************************************************************/
/******************************************************************************/

// Patient class
class Patient implements Runnable{
    DoctorsOffice q;
    int PatientID;
    boolean seenReceptionist;

    Patient(DoctorsOffice q, int i){
        this.q = q;
        PatientID = i;
        seenReceptionist = false;
        new Thread(this, "Patient "+PatientID).start();
    }

    @Override
    public void run() {
        System.out.println("Patient "+PatientID+" enters waiting room, waits for receptionist" );
        while (!seenReceptionist){
            seenReceptionist = q.getReceptionist(PatientID);
        }
        q.putWaitingRoom(PatientID);
//        System.out.println(q.getWaitingRoomListLength());
    }

    public int getPatientId(){
        return PatientID;
    }

    public void setSeenReceptionist(boolean b){
        seenReceptionist = b;
    }
}
/******************************************************************************/
/******************************************************************************/

// Receptionist class
class Receptionist implements Runnable
{
    DoctorsOffice q;


    Receptionist(DoctorsOffice q){
        this.q = q;
        new Thread(this, "Receptionist ").start();
    }

    @Override
    public void run() {
        while(true){
            q.putReceptionist();
        }
    }
}

/******************************************************************************/
/******************************************************************************/

// Nurse class -- Producer
class Nurse implements Runnable{
    DoctorsOffice q;
    int NurseID;
    Nurse(DoctorsOffice q, int i) {
        this.q = q;
        this.NurseID = i;
        new Thread(this, "Nurse").start();
    }

    @Override
    public void run() {
        while(true){
            q.nurseProduce(NurseID);
        }
    }
}

/******************************************************************************/
/******************************************************************************/

// Doctor class -- Consumer
class Doctor implements Runnable{
    DoctorsOffice q;
    int DoctorID;

    Doctor(DoctorsOffice q, int i){
        this.q = q;
        this.DoctorID = i;
        new Thread(this, "Doctor").start();
    }

    @Override
    public void run()
    {
        while(true){
            q.doctorConsume(DoctorID);
        }
    }
}

/******************************************************************************/
/******************************************************************************/

// Driver class
class PC
{
    public static void main(String args[])
    {
        // creating buffer queue
        DoctorsOffice q = new DoctorsOffice();

        for (int i = 1; i < 2; i++) {
            // starting consumer thread
            new Doctor(q, i);

            // starting producer thread
            new Nurse(q, i);
        }


        // starting receptionist thread
        new Receptionist(q);

        // starting patient thread
        for (int i = 1; i < 4; i++) {
           new Patient(q, i);
           try {
                sleep(1000);
            }
            catch (InterruptedException e) {
            }
        }
    }
}
