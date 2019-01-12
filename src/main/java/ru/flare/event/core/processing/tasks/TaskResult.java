package ru.flare.event.core.processing.tasks;


public class TaskResult
{
    private boolean complete = false;
    private String message = "";

    private TaskResult(String message, boolean complete) {
        this.message = message;
        this.complete = complete;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public String getMessage()
    {
        return message;
    }

    public static TaskResult error(String message) {
        return new TaskResult(message, false);
    }

    public static TaskResult success(String message) {
        return new TaskResult(message, true);
    }
}
