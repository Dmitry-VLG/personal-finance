package app.fintrack.cli;

import app.fintrack.core.api.AuthService;
import app.fintrack.core.api.WalletService;
import app.fintrack.core.model.Summary;
import app.fintrack.infra.runtime.SessionContext;
import app.fintrack.infra.service.AuthServiceImpl;
import app.fintrack.infra.service.WalletServiceImpl;
import app.fintrack.infra.store.FileUserRepository;
import app.fintrack.infra.store.FileWalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.Console;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class Main {
    private final Scanner in = new Scanner(System.in);

    private final SessionContext session = new SessionContext();
    private final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final AuthService auth = new AuthServiceImpl(new FileUserRepository(new File("data"), om), new FileWalletRepository(new File("data")), session);
    private final WalletService wallet = new WalletServiceImpl(session);

    public static void main(String[] args) { new Main().run(); }

    private void run() {
        println("FinTrack CLI. Введите 'help' для списка команд.");
        while (true) {
            System.out.print("fintrack> ");
            String line = safeReadLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                if (line.equals("exit")) { logoutIfNeeded(); println("Bye!"); break; }
                else if (line.equals("help")) { printHelp(); }
                else if (line.startsWith("register ")) { cmdRegister(line.substring(9).trim()); }
                else if (line.startsWith("login ")) { cmdLogin(line.substring(6).trim()); }
                else if (line.equals("logout")) { auth.logout(); println("OK"); }
                else if (line.startsWith("income ")) { cmdIncome(line.substring(7).trim()); }
                else if (line.startsWith("expense ")) { cmdExpense(line.substring(8).trim()); }
                else if (line.startsWith("budget set ")) { cmdBudgetSet(line.substring(11).trim()); }
                else if (line.startsWith("summary")) { cmdSummary(line.replaceFirst("summary", "").trim()); }
                else { println("Неизвестная команда. 'help' — список."); }
            } catch (Exception e) {
                println("Ошибка: "+ e.getMessage());
            }
        }
    }

    private void logoutIfNeeded() { try { auth.logout(); } catch (Exception ignored) {} }

    private void printHelp() {
        println("Команды:\n" +
                "  register <login>                — регистрация (пароль спросим скрыто)\n"+
                "  login <login>                   — вход (пароль спросим скрыто)\n"+
                "  logout                          — выход (сохранит кошелёк)\n"+
                "  income <category> <amount> [note]  — добавить доход\n"+
                "  expense <category> <amount> [note] — добавить расход\n"+
                "  budget set <category> <limit>     — установить бюджет по категории\n"+
                "  summary [--from yyyy-mm-dd] [--to yyyy-mm-dd] — сводка\n"+
                "  exit                            — выход из приложения");
    }

    private void cmdRegister(String login) {
        char[] pwd = readPassword("Пароль: ");
        auth.register(login, pwd);
        println("Пользователь создан");
    }

    private void cmdLogin(String login) {
        char[] pwd = readPassword("Пароль: ");
        auth.login(login, pwd);
        println("Вход выполнен");
    }

    private void cmdIncome(String args) { addTx(true, args); }
    private void cmdExpense(String args) { addTx(false, args); }

    private void addTx(boolean income, String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) { println("Формат: "+(income?"income":"expense")+" <category> <amount> [note]"); return; }
        String category = parts[0];
        BigDecimal amount = new BigDecimal(parts[1]);
        String note = args.substring(args.indexOf(parts[1]) + parts[1].length()).trim();
        if (note.isEmpty()) note = null;
        if (income) wallet.addIncome(category, amount, note); else wallet.addExpense(category, amount, note);
        println("OK");
        // Немедленные уведомления
        Summary s = wallet.getSummary(Set.of(category), null, null);
        var bs = s.budgetStatuses().get(category);
        if (bs != null && bs.exceeded()) {
            println("Внимание: превышен бюджет категории '"+category+"' на "+ bs.spent().subtract(bs.limit()));
        }
        if (s.totalExpense().compareTo(s.totalIncome()) > 0) {
            println("Внимание: общие расходы превысили доходы.");
        }
    }

    private void cmdBudgetSet(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length < 2) { println("Формат: budget set <category> <limit>"); return; }
        wallet.setBudget(parts[0], new BigDecimal(parts[1]));
        println("OK");
    }

    private void cmdSummary(String args) {
        LocalDate from = null, to = null;
        String[] parts = args.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if ("--from".equals(parts[i]) && i+1 < parts.length) from = LocalDate.parse(parts[++i]);
            else if ("--to".equals(parts[i]) && i+1 < parts.length) to = LocalDate.parse(parts[++i]);
        }
        Summary s = wallet.getSummary(Collections.emptySet(), from, to);
        println("ИТОГО: доход="+s.totalIncome()+", расход="+s.totalExpense());
        if (!s.expensesByCategory().isEmpty()) {
            println("Расходы по категориям:");
            s.expensesByCategory().forEach((k,v) -> println("  "+k+": "+v));
        }
        if (!s.budgetStatuses().isEmpty()) {
            println("Бюджеты:");
            s.budgetStatuses().forEach((k,bs) -> {
                println("  "+k+": лимит="+bs.limit()+", потрачено="+bs.spent()+", остаток="+bs.remaining()+ (bs.exceeded()?" (превышен)":""));
            });
        }
    }

    private char[] readPassword(String prompt) {
        Console c = System.console();
        if (c != null) return c.readPassword(prompt);
        System.out.print(prompt);
        return in.nextLine().toCharArray();
    }

    private String safeReadLine() { try { return in.nextLine(); } catch (Exception e) { return null; } }
    private void println(String s) { System.out.println(s); }
}
