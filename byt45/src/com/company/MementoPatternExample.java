package com.company;
import java.util.ArrayList;
import java.util.List;

class Memento {
    private String state;

    public Memento(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }
}

class Creator
{
    private String state;

    public void setState(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }

    public Memento saveStateToMemento(){
        return new Memento(state);
    }

    public void getStateFromMemento(Memento memento){
        state = memento.getState();
    }
}

class Keeper
{
    private List<Memento> mementoList = new ArrayList<Memento>();

    public void add(Memento state){
        mementoList.add(state);
    }

    public Memento get(int index){
        return mementoList.get(index);
    }
}

public class MementoPatternExample
{
    public static void main(String[] args) {

        Creator creator = new Creator();
        Keeper keeper = new Keeper();

        creator.setState("State #1");
        creator.setState("State #2");
        keeper.add(creator.saveStateToMemento());

        creator.setState("State #3");
        keeper.add(creator.saveStateToMemento());

        creator.setState("State #4");
        System.out.println("Current State: " + creator.getState());

        creator.getStateFromMemento(keeper.get(0));
        System.out.println("First saved State: " + creator.getState());
        creator.getStateFromMemento(keeper.get(1));
        System.out.println("Second saved State: " + creator.getState());
    }
}
