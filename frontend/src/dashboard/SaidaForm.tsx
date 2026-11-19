import { useEffect, useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { ApiError, registrarSaida, type Ingrediente } from '@/api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatarQuantidade, lerQuantidade } from '@/format'

const RESPONSAVEL_KEY = 'armazem.responsavel'

type SaidaFormProps = {
  ingredientes: Ingrediente[]
  ingredienteId: string
  onIngredienteId: (id: string) => void
  onSucesso: () => Promise<void>
}

export function SaidaForm({ ingredientes, ingredienteId, onIngredienteId, onSucesso }: SaidaFormProps) {
  const [responsavel, setResponsavel] = useState(() => localStorage.getItem(RESPONSAVEL_KEY) ?? '')
  const [quantidade, setQuantidade] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const selecionado = ingredientes.find((item) => String(item.id) === ingredienteId)

  useEffect(() => {
    localStorage.setItem(RESPONSAVEL_KEY, responsavel)
  }, [responsavel])

  async function enviar(event: FormEvent) {
    event.preventDefault()
    const valor = lerQuantidade(quantidade)
    if (!selecionado) {
      setErro('Escolha o ingrediente que vai sair.')
      return
    }
    if (!valor) {
      setErro('Informe uma quantidade maior que zero.')
      return
    }
    if (!responsavel.trim()) {
      setErro('Informe o responsável.')
      document.getElementById('responsavel-saida')?.focus()
      return
    }

    setEnviando(true)
    setErro(null)
    try {
      const ingrediente = await registrarSaida(selecionado.id, {
        quantidade: valor,
        responsavel: responsavel.trim(),
      })
      toast.success(`Saída registrada. Saldo de ${ingrediente.nome}: ${formatarQuantidade(ingrediente.quantidade, ingrediente.tipo)}.`)
      setQuantidade('')
      await onSucesso()
    } catch (causa) {
      setErro(causa instanceof ApiError ? causa.message : 'Não foi possível registrar a saída.')
      await onSucesso()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Card id="saida">
      <CardHeader>
        <CardTitle>Registrar saída</CardTitle>
        <CardDescription>Retira uma quantidade do saldo e guarda quem autorizou.</CardDescription>
      </CardHeader>
      <CardContent>
        {ingredientes.length === 0 ? (
          <p className="text-sm text-muted-foreground">Nenhum ingrediente cadastrado ainda.</p>
        ) : (
          <form className="grid gap-4" onSubmit={enviar}>
            <div className="grid gap-2">
              <Label htmlFor="responsavel-saida">Responsável</Label>
              <Input
                id="responsavel-saida"
                value={responsavel}
                placeholder="Nome de quem está operando"
                onChange={(event) => setResponsavel(event.target.value)}
              />
            </div>
            <div className="grid gap-2">
              <Label>Ingrediente</Label>
              <Select value={ingredienteId || undefined} onValueChange={onIngredienteId}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent position="popper">
                  {ingredientes.map((item) => (
                    <SelectItem key={item.id} value={String(item.id)}>
                      {item.nome} · {item.compartimentoCodigo} · {formatarQuantidade(item.quantidade, item.tipo)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="grid gap-2">
              <Label htmlFor="quantidade-saida">Quantidade</Label>
              <Input
                id="quantidade-saida"
                inputMode="decimal"
                placeholder="0,000"
                value={quantidade}
                onChange={(event) => setQuantidade(event.target.value)}
              />
              {selecionado ? (
                <p className="text-sm text-muted-foreground">
                  Saldo atual: {formatarQuantidade(selecionado.quantidade, selecionado.tipo)}.
                </p>
              ) : null}
            </div>
            {erro ? <p className="text-sm text-destructive">{erro}</p> : null}
            <Button type="submit" variant="secondary" disabled={enviando || !ingredienteId}>
              {enviando ? 'Registrando…' : 'Registrar saída'}
            </Button>
          </form>
        )}
      </CardContent>
    </Card>
  )
}
