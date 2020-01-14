package hr.bzg.tcp.utilities;

import hr.bzg.tcp.network.*;

public interface Action {
	public void perform(Client client);
}