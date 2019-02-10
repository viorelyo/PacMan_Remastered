package general.Repository.UserRepoTasks;

import general.Repository.UserRepository;
import javafx.concurrent.Task;

public class FindAllTask extends Task {
    private UserRepository repo;

    public FindAllTask(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    protected Object call() throws Exception {
        repo.findAll();
        return null;
    }
}
