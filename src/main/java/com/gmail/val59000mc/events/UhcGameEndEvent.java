package com.gmail.val59000mc.events;

import com.gmail.val59000mc.players.UhcPlayer;

import java.util.List;

public class UhcGameEndEvent extends UhcEvent {

    private final List<UhcPlayer> leaders;

    public UhcGameEndEvent(List<UhcPlayer> leaders) { this.leaders = leaders; }

    public List<UhcPlayer> getLeaders() { return leaders; }

}