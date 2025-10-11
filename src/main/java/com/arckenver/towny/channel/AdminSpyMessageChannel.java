package com.arckenver.towny.channel;

import com.arckenver.towny.DataHandler;
import com.arckenver.towny.object.Towny;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;

public class AdminSpyMessageChannel extends TownyMessageChannel {

    @Override
    public void send(Object sender, Text message){
        if(sender == null){
            super.send(null, message);
        }else{
            Towny senderTowny = DataHandler.getTownyOfPlayer(((Player) sender).getUniqueId());

            for(MessageReceiver receiver : getMembers()){
                final Towny receiverTowny = DataHandler.getTownyOfPlayer(((Player) receiver).getUniqueId());
                if(receiverTowny == senderTowny) continue;
                receiver.sendMessage(message);
            }
        }
    }

}
